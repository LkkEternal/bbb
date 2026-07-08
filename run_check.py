import subprocess, os, sys
sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')
os.chdir('E:/学习资料/make/navel-orange-monitoring')
result = subprocess.run(['git', 'log', '--all', '--oneline', '-15'], capture_output=True, text=True, encoding='utf-8')
print('=== ALL BRANCHES ===')
print(result.stdout[:2000] if result.stdout else '(NO OUTPUT)')
print('STDERR:', result.stderr[:500] if result.stderr else '(NONE)')
print('Return code:', result.returncode)
