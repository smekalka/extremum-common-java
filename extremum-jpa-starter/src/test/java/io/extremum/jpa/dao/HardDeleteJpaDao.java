package io.extremum.jpa.dao;

import io.extremum.jpa.dao.impl.SpringDataJpaCommonDao;
import io.extremum.jpa.model.HardDeleteJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteJpaDao extends SpringDataJpaCommonDao<HardDeleteJpaModel> {
    List<HardDeleteJpaModel> findByName(String name);
}
