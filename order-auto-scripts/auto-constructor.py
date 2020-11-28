import re

"""自动写构造函数（建议不用）"""

schema = """
vo.id = po.getId();
    vo.customerId = orderSimplePo.getCustomerId();
    vo.shopId = orderSimplePo.getShopId();
    vo.pid = orderSimplePo.getPid();
    vo.orderType = orderSimplePo.getOrderType();
    vo.freightPrice = orderSimplePo.getFreightPrice();
    vo.discountPrice = orderSimplePo.getDiscountPrice();
    vo.originPrice = orderSimplePo.getOriginPrice();
    vo.state = orderSimplePo.getState();
    vo.substate = orderSimplePo.getSubstate();
    vo.gmtCreated = orderSimplePo.getGmtCreated();
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
    pattern = re.compile(r'vo\.[a-zA-Z]+')
    f = re.findall(pattern, schema)

    res = []
    for fi in f:
        res.append('po.get' + formatter(fi.replace('vo.', '')) + "()")
    print("(" + ", ".join(res) + ")")
