#!/usr/bin/env python3
"""Measure source text from a Git revision; not coverage or cyclomatic complexity."""
import argparse,json,subprocess
from pathlib import Path
parser=argparse.ArgumentParser();parser.add_argument('--ref',default='v0.1-baseline-backend');args=parser.parse_args()
root=Path(__file__).resolve().parents[1]
def git(*args):return subprocess.check_output(['git',*args],cwd=root,text=True)
revision=git('rev-parse',args.ref+'^{commit}').strip()
paths=git('ls-tree','-r','--name-only',revision).splitlines()
result={'revision':revision,'method':'physical source lines from git show; whitespace-only lines excluded for nonblank count','groups':{}}
for name,prefix in [('production','backend/src/main/java/'),('tests','backend/src/test/java/')]:
 files=[]
 for path in paths:
  if not path.startswith(prefix) or not path.endswith('.java'):continue
  lines=git('show',revision+':'+path).splitlines()
  files.append({'path':path,'physical_lines':len(lines),'nonblank_lines':sum(bool(x.strip()) for x in lines),'longest_line_characters':max(map(len,lines),default=0)})
 result['groups'][name]={'java_files':len(files),'physical_lines':sum(x['physical_lines'] for x in files),'nonblank_lines':sum(x['nonblank_lines'] for x in files),'files':files}
result['services_importing_http']=[]
for path in paths:
 if path.startswith('backend/src/main/java/') and '/service/' in path and path.endswith('.java'):
  source=git('show',revision+':'+path)
  if 'org.springframework.http' in source or 'org.springframework.web.server' in source:result['services_importing_http'].append(path)
print(json.dumps(result,ensure_ascii=False,indent=2))
