import zlib, os, sys

def read_git_object(sha):
    path = f".git/objects/{sha[:2]}/{sha[2:]}"
    with open(path, 'rb') as f:
        compressed = f.read()
    decompressed = zlib.decompress(compressed)
    # Format: "type size\0content"
    null_idx = decompressed.index(b'\0')
    header = decompressed[:null_idx].decode()
    content = decompressed[null_idx+1:]
    return header, content

# Read master commit
os.chdir(r"E:\学习资料\make\navel-orange-monitoring")
with open(".git/refs/heads/master") as f:
    master_sha = f.read().strip()
print(f"master HEAD: {master_sha}")

hdr, content = read_git_object(master_sha)
print(f"Header: {hdr}")
print(f"Content: {content.decode()[:200]}")

# Parse tree SHA
for line in content.decode().split('\n'):
    if line.startswith('tree '):
        tree_sha = line.split()[1]
        print(f"\nTree SHA: {tree_sha}")
        break

# Read tree
hdr2, tree_content = read_git_object(tree_sha)
entries = tree_content.decode(errors='replace').split('\n')
files_found = []
for entry in entries:
    if 'ota' in entry.lower() or 'firmware' in entry.lower() or '固件' in entry:
        files_found.append(entry)

print(f"\nOTA-related files in tree: {files_found}")

# Search all tree entries for OTA
print("\nSearching all tree entries for OTA...")
for entry in entries:
    if entry.strip():
        parts = entry.split('\t')
        if len(parts) >= 2 and ('ota' in parts[-1].lower() or 'firmware' in parts[-1].lower()):
            files_found.append(entry)

print(f"All entries found: {len(files_found)}")
for f in files_found[:20]:
    print(f"  {f}")
