import subprocess, os, sys
sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')
os.chdir('E:/学习资料/make/navel-orange-monitoring')
result = subprocess.run(['git', 'log', '--all', '--oneline', '-15'], capture_output=True, text=True, encoding='utf-8')
with open('output.txt', 'w', encoding='utf-8') as f:
    f.write('=== ALL BRANCHES ===\n')
    f.write(result.stdout[:2000] if result.stdout else '(NO OUTPUT)')
    f.write('\n')
    f.write('STDERR:')
    f.write(result.stderr[:500] if result.stderr else '(NONE)')
    f.write('\n')
    f.write('Return code: ' + str(result.returncode))
