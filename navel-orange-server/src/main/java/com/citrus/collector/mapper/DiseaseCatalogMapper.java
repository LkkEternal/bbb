package com.citrus.collector.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.collector.entity.DiseaseCatalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 病害字典 Mapper
 */
@Mapper
public interface DiseaseCatalogMapper extends BaseMapper<DiseaseCatalog> {

    /**
     * 根据病害编码查询
     */
    @Select("SELECT * FROM disease_catalog WHERE disease_code = #{code}")
    DiseaseCatalog selectByCode(@Param("code") String diseaseCode);
}
