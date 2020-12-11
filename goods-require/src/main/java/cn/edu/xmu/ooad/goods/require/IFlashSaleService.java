package cn.edu.xmu.ooad.goods.require;

public interface IFlashSaleService {

    /**
     * 我们设置定时器 前十分钟调用此接口将信息传过去 实现类可以load redis
     */
    void loadFlashSale(Long flashSaleId);
}
