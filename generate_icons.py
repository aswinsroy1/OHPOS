from PIL import Image
import os

bg_path = "ic_launcher_background.png"
fg_path = "ic_launcher_foreground.png"

bg = Image.open(bg_path).convert("RGBA")
fg = Image.open(fg_path).convert("RGBA")

# Adaptive Icon Layer Sizes (mdpi is 108x108)
adaptive_sizes = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432
}

# Legacy Icon Sizes (mdpi is 48x48)
legacy_sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

res_dir = "app/src/main/res"

for density, ad_size in adaptive_sizes.items():
    d_path = os.path.join(res_dir, density)
    os.makedirs(d_path, exist_ok=True)
    
    # Resize bg and fg
    bg_resized = bg.resize((ad_size, ad_size), Image.Resampling.LANCZOS)
    fg_resized = fg.resize((ad_size, ad_size), Image.Resampling.LANCZOS)
    
    bg_resized.save(os.path.join(d_path, "ic_launcher_background.png"))
    fg_resized.save(os.path.join(d_path, "ic_launcher_foreground.png"))
    
for density, leg_size in legacy_sizes.items():
    d_path = os.path.join(res_dir, density)
    os.makedirs(d_path, exist_ok=True)
    
    # Legacy composites are LegSize x LegSize
    # Adaptive layers have 1/6th padding on each side to reach 108dp. 
    # The actual icon is 72dp. LegSize is 48dp, which maps to 72dp for the "safe zone" of adaptive icon.
    # To composite: resize to the adaptive size (e.g. 108x108 for mdpi), composite them, then crop/resize?
    # Wait, simple way: composite bg and fg (at ad_size), then resize the composited to leg_size. Wait, no. The ad_size includes the "bleed" (108dp). 
    # Legacy icon is exactly 48dp which corresponds to the 72dp safe zone. So if we composite at 108x108, we should crop the center 72x72, then resize that to 48x48.
    
    # ad_size (e.g., 108) -> crop center 72 -> resize to 48
    ad_size = adaptive_sizes[density]
    
    bg_resized = bg.resize((ad_size, ad_size), Image.Resampling.LANCZOS)
    fg_resized = fg.resize((ad_size, ad_size), Image.Resampling.LANCZOS)
    
    composite = Image.alpha_composite(bg_resized, fg_resized)
    
    # Crop to safe zone (72dp out of 108dp, so 72/108 = 2/3 of the size)
    safe_zone = ad_size * 2 // 3
    margin = (ad_size - safe_zone) // 2
    
    cropped = composite.crop((margin, margin, margin + safe_zone, margin + safe_zone))
    legacy = cropped.resize((leg_size, leg_size), Image.Resampling.LANCZOS)
    
    legacy.save(os.path.join(d_path, "ic_launcher.png"))
    
    # For round icon, we apply a circular mask to the legacy icon
    mask = Image.new('L', (leg_size, leg_size), 0)
    import ImageDraw
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, leg_size, leg_size), fill=255)
    
    round_icon = legacy.copy()
    round_icon.putalpha(mask)
    
    round_icon.save(os.path.join(d_path, "ic_launcher_round.png"))

print("Done generating icons.")
