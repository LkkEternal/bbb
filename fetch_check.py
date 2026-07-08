import subprocess, os
os.chdir(r"E:\学习资料\make\navel-orange-monitoring")

# Force fetch to update all tracking refs
r = subprocess.run(["git", "fetch", "--all", "--force", "--prune"], capture_output=True, text=True)
print("=== FETCH ===")
print("STDOUT:", r.stdout)
print("STDERR:", r.stderr)
print("RC:", r.returncode)

# Check master commit
with open(".git/refs/heads/master") as f:
    print(f"LOCAL master: {f.read().strip()}")

# Check origin/master commit
try:
    with open(".git/refs/remotes/origin/master") as f:
        print(f"ORIGIN master: {f.read().strip()}")
except:
    print("ORIGIN master ref not found")

# Check origin/miniapp
try:
    with open(".git/refs/remotes/origin/miniapp") as f:
        print(f"ORIGIN miniapp: {f.read().strip()}")
except:
    print("ORIGIN miniapp ref not found")

# List all remote heads
r2 = subprocess.run(["git", "ls-remote", "--heads", "origin"], capture_output=True, text=True)
print(f"\n=== REMOTE HEADS ===")
print(r2.stdout)
print(r2.stderr if r2.stderr else "")
