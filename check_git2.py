import subprocess, os
os.chdir('E:/学习资料/make/navel-orange-monitoring')
result = subprocess.run(['git', 'log', '--all', '--oneline', '-15'], capture_output=True, text=True)
print("=== ALL BRANCHES ===")
print(result.stdout[:2000] if result.stdout else "(NO OUTPUT)")
print("STDERR:", result.stderr[:500] if result.stderr else "(NONE)")
