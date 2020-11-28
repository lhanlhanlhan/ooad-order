import re

"""
将 API 的属性定义 转为 Vo 对象的属性定义
"""

string = """
"consignee": "string",
  "regionId": 0,
  "address": "string",
  "mobile": "string"
"""

if __name__ == '__main__':
    pattern = re.compile(r'".*?"')
    fields = re.findall(pattern, string)
    fields.append('"long"')
    i = 0
    while i < len(fields)-1:
        # 获取属性
        attr = fields[i]
        # 检查属性的类型
        attr_class = fields[i+1]
        if attr_class == '''"string"''':
            print("private String " + attr.replace('"', '') + ";")
            i += 1
        else:
            print("private Long " + attr.replace('"', '') + ";")
        i += 1
