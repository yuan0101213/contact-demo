"""对运行中的真实后端做接口验证，仅删除本脚本创建的数据。"""
import json
import sys
import uuid
from urllib.error import HTTPError
from urllib.parse import quote
from urllib.request import Request, urlopen

base = (sys.argv[1] if len(sys.argv) > 1 else 'http://127.0.0.1:8080').rstrip('/')


def call(method, path, payload=None, expected=200):
    body = None if payload is None else json.dumps(payload).encode()
    request = Request(base + '/api/contacts' + path, data=body,
                      headers={'Content-Type': 'application/json'}, method=method)
    try:
        with urlopen(request, timeout=10) as response:
            status, data = response.status, response.read()
    except HTTPError as error:
        status, data = error.code, error.read()
    assert status == expected, (method, path, status, data)
    return json.loads(data) if data else None


marker = 'smoke-' + uuid.uuid4().hex[:12]
payload = {'name': '接口测试', 'email': marker + '@example.com',
           'phone': '13812345678', 'note': '中文 🌱 ' + marker}
record = call('POST', '', payload, 201)
try:
    record_id = record['id']
    path = '/' + str(record_id)
    assert call('GET', path)['note'] == payload['note']
    assert any(row['id'] == record_id for row in call('GET', '?keyword=' + marker))
    payload['name'] = '修改成功'
    assert call('PUT', path, payload)['name'] == payload['name']
    assert call('PUT', path, payload)['name'] == payload['name'], '原值保存也应成功'
    assert call('GET', path)['name'] == payload['name']
    call('POST', '', {**payload, 'email': 'bad-email'}, 400)
    call('POST', '', {**payload, 'name': '   '}, 400)
    call('POST', '', {**payload, 'phone': '123'}, 400)
    call('POST', '', {**payload, 'note': 'a' * 101}, 400)
    assert not call('GET', '?keyword=' + quote("' OR 1=1 -- " + marker))
    call('DELETE', path, expected=204)
    call('GET', path, expected=404)
    call('PUT', path, payload, 404)
    call('DELETE', path, expected=404)
    print('PASS: 新增、单条查询、搜索、编辑、原值保存、删除、校验、404 和参数化查询。')
finally:
    # 如果中途断言失败，也尽量清理本测试创建的记录。
    call('DELETE', '/' + str(record['id']), expected=204 if call('GET', '?keyword=' + marker) else 404)
