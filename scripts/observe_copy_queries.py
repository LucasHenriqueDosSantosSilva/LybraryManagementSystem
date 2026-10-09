#!/usr/bin/env python3
"""Observe Hibernate SQL for isolated local requests; requires DEBUG SQL logging."""
import argparse,json,time,uuid,urllib.request,urllib.error,subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--log',required=True);p.add_argument('--output',required=True);args=p.parse_args()
log=Path(args.log);base='http://127.0.0.1:8080/api/';created=[]
def request(method,path,data=None):
 r=urllib.request.Request(base+path,data=json.dumps(data).encode() if data is not None else None,headers={'Content-Type':'application/json'},method=method)
 try:
  with urllib.request.urlopen(r,timeout=5) as response:return response.status,json.loads(response.read() or 'null')
 except urllib.error.HTTPError as e:return e.code,json.loads(e.read())
def create(path,data):
 status,body=request('POST',path,data)
 if status!=201:raise RuntimeError(f'Create {path} failed ({status}); no pre-existing data will be removed.')
 created.append(f"{path}/{body['id']}");return body['id']
for _ in range(20):
 try:
  if request('GET','copies')[0]==200:break
 except OSError:pass
 time.sleep(1)
else:raise RuntimeError('API unavailable')
result={'revision':subprocess.check_output(['git','rev-parse','HEAD'],cwd=Path(__file__).resolve().parents[1],text=True).strip(),'method':'count org.hibernate.SQL SELECT log events around one HTTP request; isolated process, fresh persistence context per request','samples':[]}
try:
 marker='Obs-'+uuid.uuid4().hex[:16]
 category=create('categories',{'name':marker});author=create('authors',{'name':marker})
 if request('GET','copies')[1]['page']['totalElements']!=0:raise RuntimeError('Use an empty development copy dataset for this observation; existing data will not be deleted')
 book=create('books',{'isbn':'9780306406157','title':marker,'publicationYear':2000,'categoryId':category,'authorIds':[author]})
 for i in range(3):create('copies',{'inventoryCode':marker+str(i),'bookId':book})
 for size in [1,2,3]:
  offset=log.stat().st_size
  status,body=request('GET',f'copies?size={size}')
  assert status==200 and len(body['content'])==size
  with log.open('rb') as f:f.seek(offset);lines=f.read().decode().splitlines()
  statements=[line.split(' : ',1)[-1] for line in lines if 'org.hibernate.SQL' in line and 'select ' in line.lower()]
  if not statements:raise RuntimeError('Enable Hibernate SQL DEBUG logging before observation')
  result['samples'].append({'page_size':size,'returned_copies':size,'select_events':len(statements),'statements':statements})
 Path(args.output).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
 print([(s['returned_copies'],s['select_events']) for s in result['samples']])
finally:
 for path in reversed(created):
  status,_=request('DELETE',path)
  if status not in (204,404):raise RuntimeError(f'Fixture cleanup failed for {path}: {status}')
