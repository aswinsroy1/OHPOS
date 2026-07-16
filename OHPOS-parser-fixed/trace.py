import math

pageWidth = 1000
pageWidthTolerance = pageWidth * 0.15

lines = [
    {"text": "THE COLD MENU", "left": 400, "top": 50},
    {"text": "F", "left": 100, "top": 120},
    {"text": "10.00", "left": 130, "top": 120},
    {"text": "Ceviche", "left": 100, "top": 180},
    {"text": "15.00", "left": 200, "top": 180},
    {"text": "Fresh Salad", "left": 600, "top": 125},
    {"text": "12.00", "left": 800, "top": 125},
    {"text": "Oysters", "left": 600, "top": 180},
    {"text": "20.00", "left": 750, "top": 180}
]

print("--- START TRACE (Multi-Column Cold Menu) ---")
clusters = []

for line in lines:
    left = line["left"]
    placed = False
    for cluster in clusters:
        cluster_left = sum(l["left"] for l in cluster) / len(cluster)
        if abs(left - cluster_left) <= pageWidthTolerance:
            cluster.append(line)
            placed = True
            break
    if not placed:
        clusters.append([line])

print(f"Total Clusters Found: {len(clusters)}")
for i, c in enumerate(clusters):
    avg_left = sum(l["left"] for l in c) / len(c)
    print(f"Cluster {i+1} (avg left: {avg_left:.1f}):")
    for l in c:
        print(f"  - '{l['text']}' (Left: {l['left']}, Top: {l['top']})")

actual_columns = sorted([c for c in clusters if len(c) > 2], key=lambda c: sum(l["left"] for l in c)/len(c))
print(f"\nActual Columns Identified (size > 2): {len(actual_columns)}")

header_lines = sorted([l for c in clusters if len(c) <= 2 for l in c], key=lambda l: l["top"])
print("\nHeaders Identified:")
for h in header_lines:
    print(f"  - '{h['text']}'")

print("\n--- START TRACE (Single Column Fallback) ---")
single_lines = [
    {"text": "THE HOT MENU", "left": 100, "top": 50},
    {"text": "Burger 15.00", "left": 100, "top": 120},
    {"text": "Fries 5.00", "left": 100, "top": 180}
]
s_clusters = []
for line in single_lines:
    left = line["left"]
    placed = False
    for cluster in s_clusters:
        cluster_left = sum(l["left"] for l in cluster) / len(cluster)
        if abs(left - cluster_left) <= pageWidthTolerance:
            cluster.append(line)
            placed = True
            break
    if not placed:
        s_clusters.append([line])

print(f"Single Menu Clusters Found: {len(s_clusters)}")
if len(s_clusters) <= 1:
    print("Result: Successfully fell back to flat parser.")
