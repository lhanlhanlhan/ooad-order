package cn.edu.xmu.ooad.order.model.po;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PieceFreightModelPoExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public PieceFreightModelPoExample() {
        oredCriteria = new ArrayList<>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdIsNull() {
            addCriterion("freight_model_id is null");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdIsNotNull() {
            addCriterion("freight_model_id is not null");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdEqualTo(Long value) {
            addCriterion("freight_model_id =", value, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdNotEqualTo(Long value) {
            addCriterion("freight_model_id <>", value, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdGreaterThan(Long value) {
            addCriterion("freight_model_id >", value, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdGreaterThanOrEqualTo(Long value) {
            addCriterion("freight_model_id >=", value, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdLessThan(Long value) {
            addCriterion("freight_model_id <", value, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdLessThanOrEqualTo(Long value) {
            addCriterion("freight_model_id <=", value, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdIn(List<Long> values) {
            addCriterion("freight_model_id in", values, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdNotIn(List<Long> values) {
            addCriterion("freight_model_id not in", values, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdBetween(Long value1, Long value2) {
            addCriterion("freight_model_id between", value1, value2, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFreightModelIdNotBetween(Long value1, Long value2) {
            addCriterion("freight_model_id not between", value1, value2, "freightModelId");
            return (Criteria) this;
        }

        public Criteria andFirstItemsIsNull() {
            addCriterion("first_items is null");
            return (Criteria) this;
        }

        public Criteria andFirstItemsIsNotNull() {
            addCriterion("first_items is not null");
            return (Criteria) this;
        }

        public Criteria andFirstItemsEqualTo(Integer value) {
            addCriterion("first_items =", value, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsNotEqualTo(Integer value) {
            addCriterion("first_items <>", value, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsGreaterThan(Integer value) {
            addCriterion("first_items >", value, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsGreaterThanOrEqualTo(Integer value) {
            addCriterion("first_items >=", value, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsLessThan(Integer value) {
            addCriterion("first_items <", value, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsLessThanOrEqualTo(Integer value) {
            addCriterion("first_items <=", value, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsIn(List<Integer> values) {
            addCriterion("first_items in", values, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsNotIn(List<Integer> values) {
            addCriterion("first_items not in", values, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsBetween(Integer value1, Integer value2) {
            addCriterion("first_items between", value1, value2, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsNotBetween(Integer value1, Integer value2) {
            addCriterion("first_items not between", value1, value2, "firstItems");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceIsNull() {
            addCriterion("first_items_price is null");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceIsNotNull() {
            addCriterion("first_items_price is not null");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceEqualTo(Long value) {
            addCriterion("first_items_price =", value, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceNotEqualTo(Long value) {
            addCriterion("first_items_price <>", value, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceGreaterThan(Long value) {
            addCriterion("first_items_price >", value, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceGreaterThanOrEqualTo(Long value) {
            addCriterion("first_items_price >=", value, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceLessThan(Long value) {
            addCriterion("first_items_price <", value, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceLessThanOrEqualTo(Long value) {
            addCriterion("first_items_price <=", value, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceIn(List<Long> values) {
            addCriterion("first_items_price in", values, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceNotIn(List<Long> values) {
            addCriterion("first_items_price not in", values, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceBetween(Long value1, Long value2) {
            addCriterion("first_items_price between", value1, value2, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andFirstItemsPriceNotBetween(Long value1, Long value2) {
            addCriterion("first_items_price not between", value1, value2, "firstItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsIsNull() {
            addCriterion("additional_items is null");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsIsNotNull() {
            addCriterion("additional_items is not null");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsEqualTo(Integer value) {
            addCriterion("additional_items =", value, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsNotEqualTo(Integer value) {
            addCriterion("additional_items <>", value, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsGreaterThan(Integer value) {
            addCriterion("additional_items >", value, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsGreaterThanOrEqualTo(Integer value) {
            addCriterion("additional_items >=", value, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsLessThan(Integer value) {
            addCriterion("additional_items <", value, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsLessThanOrEqualTo(Integer value) {
            addCriterion("additional_items <=", value, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsIn(List<Integer> values) {
            addCriterion("additional_items in", values, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsNotIn(List<Integer> values) {
            addCriterion("additional_items not in", values, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsBetween(Integer value1, Integer value2) {
            addCriterion("additional_items between", value1, value2, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsNotBetween(Integer value1, Integer value2) {
            addCriterion("additional_items not between", value1, value2, "additionalItems");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceIsNull() {
            addCriterion("additional_items_price is null");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceIsNotNull() {
            addCriterion("additional_items_price is not null");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceEqualTo(Long value) {
            addCriterion("additional_items_price =", value, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceNotEqualTo(Long value) {
            addCriterion("additional_items_price <>", value, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceGreaterThan(Long value) {
            addCriterion("additional_items_price >", value, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceGreaterThanOrEqualTo(Long value) {
            addCriterion("additional_items_price >=", value, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceLessThan(Long value) {
            addCriterion("additional_items_price <", value, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceLessThanOrEqualTo(Long value) {
            addCriterion("additional_items_price <=", value, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceIn(List<Long> values) {
            addCriterion("additional_items_price in", values, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceNotIn(List<Long> values) {
            addCriterion("additional_items_price not in", values, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceBetween(Long value1, Long value2) {
            addCriterion("additional_items_price between", value1, value2, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andAdditionalItemsPriceNotBetween(Long value1, Long value2) {
            addCriterion("additional_items_price not between", value1, value2, "additionalItemsPrice");
            return (Criteria) this;
        }

        public Criteria andRegionIdIsNull() {
            addCriterion("region_id is null");
            return (Criteria) this;
        }

        public Criteria andRegionIdIsNotNull() {
            addCriterion("region_id is not null");
            return (Criteria) this;
        }

        public Criteria andRegionIdEqualTo(Long value) {
            addCriterion("region_id =", value, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdNotEqualTo(Long value) {
            addCriterion("region_id <>", value, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdGreaterThan(Long value) {
            addCriterion("region_id >", value, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdGreaterThanOrEqualTo(Long value) {
            addCriterion("region_id >=", value, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdLessThan(Long value) {
            addCriterion("region_id <", value, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdLessThanOrEqualTo(Long value) {
            addCriterion("region_id <=", value, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdIn(List<Long> values) {
            addCriterion("region_id in", values, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdNotIn(List<Long> values) {
            addCriterion("region_id not in", values, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdBetween(Long value1, Long value2) {
            addCriterion("region_id between", value1, value2, "regionId");
            return (Criteria) this;
        }

        public Criteria andRegionIdNotBetween(Long value1, Long value2) {
            addCriterion("region_id not between", value1, value2, "regionId");
            return (Criteria) this;
        }

        public Criteria andGmtCreateIsNull() {
            addCriterion("gmt_create is null");
            return (Criteria) this;
        }

        public Criteria andGmtCreateIsNotNull() {
            addCriterion("gmt_create is not null");
            return (Criteria) this;
        }

        public Criteria andGmtCreateEqualTo(LocalDateTime value) {
            addCriterion("gmt_create =", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateNotEqualTo(LocalDateTime value) {
            addCriterion("gmt_create <>", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateGreaterThan(LocalDateTime value) {
            addCriterion("gmt_create >", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("gmt_create >=", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateLessThan(LocalDateTime value) {
            addCriterion("gmt_create <", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("gmt_create <=", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateIn(List<LocalDateTime> values) {
            addCriterion("gmt_create in", values, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateNotIn(List<LocalDateTime> values) {
            addCriterion("gmt_create not in", values, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("gmt_create between", value1, value2, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("gmt_create not between", value1, value2, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedIsNull() {
            addCriterion("gmt_modified is null");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedIsNotNull() {
            addCriterion("gmt_modified is not null");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedEqualTo(LocalDateTime value) {
            addCriterion("gmt_modified =", value, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedNotEqualTo(LocalDateTime value) {
            addCriterion("gmt_modified <>", value, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedGreaterThan(LocalDateTime value) {
            addCriterion("gmt_modified >", value, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("gmt_modified >=", value, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedLessThan(LocalDateTime value) {
            addCriterion("gmt_modified <", value, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("gmt_modified <=", value, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedIn(List<LocalDateTime> values) {
            addCriterion("gmt_modified in", values, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedNotIn(List<LocalDateTime> values) {
            addCriterion("gmt_modified not in", values, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("gmt_modified between", value1, value2, "gmtModified");
            return (Criteria) this;
        }

        public Criteria andGmtModifiedNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("gmt_modified not between", value1, value2, "gmtModified");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table piece_freight_model
     *
     * @mbg.generated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table piece_freight_model
     *
     * @mbg.generated
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}