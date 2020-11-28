
import re

"""
自动将类中定义的属性转为构造函数中的赋值语句 （如有）
"""

string = """
private Long id;
    private Map<String, Object> customer;
    private Map<String, Object> shop;
    private Long pid;
    private Long orderType;
    private Long state;
    private Long subState;
    private Long gmtCreate;
    private Long originPrice;
    private Long discountPrice;
    private Long freightPrice;
    private String message;
    private Long regionId;
    private String address;
    private String mobile;
    private String consignee;
    private Long couponId;
    private Long grouponId;
    private List<OrderItemVo> orderItems;
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
    pattern = re.compile(r'(?P<name>\w+);')

    # 将匹配的字符串改成需要的字符串
    def fun(matched):
        value = matched.group('name')
        formatted = formatter(value)
        subbed = "this." + value + " = orderBo.get" + formatted + "();"
        print(subbed)
        return subbed

    re.sub(pattern, fun, string)
