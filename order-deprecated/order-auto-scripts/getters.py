import re

"""
自动地把属性定义转为 getter
"""

string = """
private Long skuId;
    private Long orderId;
    private String name;
    private Integer quantity;
    private Long price;
    private Long discount;
    private Long couponActId;
    private Long beSharedId;
    """

def formatter(src: str, firstUpper: bool = True):
    """
    将下划线分隔的名字,转换为驼峰模式
    :param src:
    :param firstUpper: 转换后的首字母是否指定大写(如
    :return:
    """
    arr = src.split('_')
    res = ''
    for i in arr:
        res = res + i[0].upper() + i[1:]

    if not firstUpper:
        res = res[0].lower() + res[1:]
    return res


if __name__ == '__main__':
    pattern = re.compile(r'(?P<type>\w+) (?P<name>\w+);')

    # 将匹配的字符串改成需要的字符串
    def fun(matched):
        t = matched.group('type')
        value = matched.group('name')
        formatted = formatter(value)
        name = "public " + t + " get" + formatted + "() {\n" + "    return orderItemPo.get" + formatted + "();\n}"
        print(name)
        return name

    re.sub(pattern, fun, string)