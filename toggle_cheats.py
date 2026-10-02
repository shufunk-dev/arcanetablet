import gzip
import shutil
import os

level_dat_path = r"C:\Users\shufu\curseforge\minecraft\Instances\My Mod\saves\Titanium Blocks Added\level.dat"
backup_path = level_dat_path + ".backup"

if not os.path.exists(level_dat_path):
    print(f"Error: {level_dat_path} does not exist.")
    exit(1)

# Create backup
shutil.copy2(level_dat_path, backup_path)
print(f"Created backup at {backup_path}")

# Read gzipped level.dat
with gzip.open(level_dat_path, 'rb') as f:
    data = bytearray(f.read())

# Search for b"allowCommands"
# In NBT, Tag Byte (0x01) followed by string length (13 = 0x00, 0x0D) and "allowCommands" and then 1 byte value (0x01 or 0x00)
tag_pattern = b"allowCommands"
pos = data.find(tag_pattern)

if pos != -1:
    val_pos = pos + len(tag_pattern)
    old_val = data[val_pos]
    print(f"Found 'allowCommands' at offset {pos}, current value: {old_val}")
    data[val_pos] = 0
    print(f"Updated 'allowCommands' to 0 (Cheats OFF).")

    with gzip.open(level_dat_path, 'wb') as f:
        f.write(data)
    print("Successfully saved level.dat with cheats disabled!")
else:
    print("Could not find 'allowCommands' tag in level.dat.")
