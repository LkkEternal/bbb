import subprocess, os
os.chdir(r"E:\学习资料\make\navel-orange-monitoring")
# Try fetching the remote info
r = subprocess.run(["git", "ls-remote", "--heads", "origin"], capture_output=True, text=True)
print("=== Remote heads ===")
print(r.stdout)
print(r.stderr)

# Check what the remote HEAD points to
r2 = subprocess.run(["git", "ls-remote", "--symref", "origin", "HEAD"], capture_output=True, text=True)
print("=== Remote HEAD ===")
print(r2.stdout)
print(r2.stderr)
