import subprocess, os, sys
os.chdir(r"E:\学习资料\make\navel-orange-monitoring")

def run(cmd):
    r = subprocess.run(cmd, capture_output=True, text=False, shell=True)
    # 用 utf-8 解码，忽略错误
    out = r.stdout.decode('utf-8', errors='replace') if r.stdout else ''
    err = r.stderr.decode('utf-8', errors='replace') if r.stderr else ''
    return out, err, r.returncode

steps = [
    "git fetch origin +refs/heads/master:refs/remotes/origin/master",
    "git log origin/master --oneline -5",
    "git checkout master",
    "git merge origin/master --no-edit",
    "git log master --oneline -5",
    "git status --short",
]

out_lines = []
for i, cmd in enumerate(steps, 1):
    out, err, rc = run(cmd)
    out_lines.append(f"=== Step {i}: {cmd} ===")
    out_lines.append(f"RC={rc}")
    if out.strip():
        out_lines.append("STDOUT:")
        out_lines.append(out.strip())
    if err.strip():
        out_lines.append("STDERR:")
        out_lines.append(err.strip())
    out_lines.append("")

# Check for OTA files - use find instead of dir to avoid encoding issues
ota_out, ota_err, ota_rc = run('dir /s /b *ota* *OTA* *firmware* 2>nul')
out_lines.append("=== OTA Files (glob) ===")
if ota_out.strip():
    out_lines.append(ota_out.strip())
else:
    out_lines.append("(none)")

# Use git to find OTA-related files
git_out, _, _ = run('git ls-files *ota* *OTA* *firmware*')
out_lines.append("=== OTA Files (git tracked) ===")
if git_out.strip():
    out_lines.append(git_out.strip())
else:
    out_lines.append("(none tracked in git)")

# Grep for OTA references
grep_out, _, _ = run('git grep -il "ota_update\\|OTA\\|firmware" -- "*.java" "*.vue" "*.ts" "*.js" "*.sql" 2>nul || echo none')
out_lines.append("=== Files containing OTA references ===")
if grep_out.strip():
    out_lines.append(grep_out.strip())
else:
    out_lines.append("(none found)")

result = "\n".join(out_lines)
print(result)

with open("pull_result.txt", "w", encoding="utf-8") as f:
    f.write(result)
