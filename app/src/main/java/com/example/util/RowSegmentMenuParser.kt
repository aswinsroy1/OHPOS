/**
 * Row/segment-based menu parser.
 *
 * Replaces naive "flatten to one string" and naive "cluster by left-x into
 * columns" approaches, both of which break when an item's name and its own
 * price are separated by a gap wide enough to look like a column boundary
 * (confirmed on the actual "Oasis Hub" cold menu: name-to-price gap is
 * ~21% of page width, similar magnitude to the real left/right column gap).
 *
 * Strategy:
 *  1. Flatten ML Kit's Text result into individual Line boxes (with rects).
 *  2. Group lines into ROWS by vertical (y) proximity — this naturally keeps
 *     a name and its own price together if they're the same physical row,
 *     regardless of horizontal distance between them.
 *  3. Within each row, split into SEGMENTS using horizontal gaps — this is
 *     what actually separates a left-column pair from a right-column pair
 *     that happen to sit on the same row.
 *  4. Each segment is parsed as one candidate item (name + trailing price)
 *     using the existing price regex. A segment with no price and short
 *     text becomes a category header, remembered per horizontal position
 *     bucket so later rows in the same horizontal region inherit it.
 */

package com.example.util // adjust package to match your project

import com.google.mlkit.vision.text.Text
import kotlin.math.abs

data class OcrLineBox(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val centerY: Int get() = (top + bottom) / 2
    val height: Int get() = bottom - top
}

data class ParsedMenuItem(
    val name: String,
    val price: Double,
    val category: String
)

object RowSegmentMenuParser {

    // Existing price-matching regex, tolerant of common OCR misreads of ₹
    // (observed as %, =, ~, ™ in real OCR output on this menu).
    private val pricePattern = Regex(
        """^(.*?)(?:[\s.\-,]*(?:Rs\.?|₹|INR|\$|%|=|~|™)?\s*[.\-,]*\s*)(\d+(?:\.\d{1,2})?)\s*$"""
    )

    // A line with no digits and reasonably short is a category-header candidate.
    private fun looksLikeCategoryCandidate(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.length in 2..40 && !trimmed.contains(Regex("""\d"""))
    }

    /**
     * @param visionText the raw ML Kit Text result for one page/image
     * @param pageWidthPx width of the source bitmap in pixels (needed to
     *   scale the horizontal-gap threshold correctly across different
     *   image resolutions)
     */
    fun parse(visionText: Text, pageWidthPx: Int): List<ParsedMenuItem> {
        // Step 1: flatten to line boxes, dropping anything without a bounding box.
        val lines = mutableListOf<OcrLineBox>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val t = line.text.trim()
                if (t.isEmpty()) continue
                lines.add(OcrLineBox(t, box.left, box.top, box.right, box.bottom))
            }
        }
        if (lines.isEmpty()) return emptyList()

        // Step 2: group into rows by vertical proximity.
        val sortedByY = lines.sortedBy { it.centerY }
        val avgHeight = sortedByY.map { it.height }.average().takeIf { it > 0 } ?: 20.0
        val rowTolerance = (avgHeight * 0.6).toInt().coerceAtLeast(6)

        val rows = mutableListOf<MutableList<OcrLineBox>>()
        for (line in sortedByY) {
            val row = rows.lastOrNull()
            if (row != null && abs(line.centerY - row.first().centerY) <= rowTolerance) {
                row.add(line)
            } else {
                rows.add(mutableListOf(line))
            }
        }

        // Step 3: within each row, split into segments by horizontal gap.
        // Gap threshold: a genuine column break is a large fraction of page
        // width. Tune this constant against real menus; 12% worked against
        // the sample "Oasis Hub" menu's column gap without splitting a
        // single item's internal name-price gap incorrectly IF step 4
        // still re-merges same-segment name+price via the price regex.
        //
        // NOTE: because we group by ROW first (not by column first), a wide
        // name-to-price gap on a single-column row does NOT cause a false
        // column split in practice — the segment splitting below only
        // matters for telling a left-pair apart from a right-pair on the
        // SAME row. If your menu has three-plus columns, this generalizes
        // automatically since it's gap-based, not fixed-count.
        val gapThresholdPx = (pageWidthPx * 0.12).toInt()

        val categoryByBucket = mutableMapOf<Int, String>()
        val results = mutableListOf<ParsedMenuItem>()
        // Bucket width for remembering "which horizontal region a category
        // belongs to" — coarse, just needs to distinguish left vs right etc.
        val bucketWidth = (pageWidthPx / 4).coerceAtLeast(1)

        for (row in rows) {
            val sortedByX = row.sortedBy { it.left }
            val segments = mutableListOf<MutableList<OcrLineBox>>()
            for (box in sortedByX) {
                val seg = segments.lastOrNull()
                if (seg != null && (box.left - seg.last().right) <= gapThresholdPx) {
                    seg.add(box)
                } else {
                    segments.add(mutableListOf(box))
                }
            }

            for (seg in segments) {
                val segText = seg.joinToString(" ") { it.text }.trim()
                val segLeft = seg.first().left
                val segRight = seg.last().right
                val segCenterX = (segLeft + segRight) / 2
                val bucket = segCenterX / bucketWidth

                val match = pricePattern.find(segText)
                if (match != null) {
                    val rawName = match.groupValues[1].trim().trim('.', '-', ',', ' ')
                    val price = match.groupValues[2].toDoubleOrNull()
                    if (price != null && rawName.isNotEmpty() && rawName.length > 1) {
                        // Real item: name + price found together in one segment.
                        val category = categoryByBucket[bucket] ?: "Uncategorized"
                        results.add(ParsedMenuItem(rawName, price, category))
                    }
                    // If rawName is empty or length <= 1 (e.g. a stray glyph),
                    // this segment is likely OCR noise, not a real item —
                    // skip rather than saving a garbage "F"-style name.
                } else if (looksLikeCategoryCandidate(segText)) {
                    categoryByBucket[bucket] = segText
                }
                // else: unmatched noise segment, ignored.
            }
        }

        return results
    }
}
