import re

"""加final"""

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

if __name__ == '__main__':
    pattern = re.compile(r'private')
    str_replaced = re.sub(pattern, "private final", string)
    print(str_replaced)
