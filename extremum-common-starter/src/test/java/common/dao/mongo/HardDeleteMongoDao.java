package common.dao.mongo;

import io.extremum.mongo.dao.impl.SpringDataMongoCommonDao;
import models.HardDeleteMongoModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteMongoDao extends SpringDataMongoCommonDao<HardDeleteMongoModel> {
    List<HardDeleteMongoModel> findByName(String name);

    long countByName(String name);
}
