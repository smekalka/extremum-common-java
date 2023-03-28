package io.extremum.jpa.repository;

import io.extremum.jpa.model.PostgresBasicModel;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import javax.persistence.EntityManager;

/**
 * @author rpuch
 */
public class ExtremumJpaRepositoryFactory extends JpaRepositoryFactory {
    private final JpaSoftDeletion jpaSoftDeletion = new JpaSoftDeletion();

    public ExtremumJpaRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (!PostgresBasicModel.class.isAssignableFrom(metadata.getDomainType())) {
            return SimpleJpaRepository.class;
        }
        if (jpaSoftDeletion.supportsSoftDeletion(metadata.getDomainType())) {
            return SoftDeleteJpaRepository.class;
        } else {
            return HardDeleteJpaRepository.class;
        }
    }

    @Override
    protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
        return super.getTargetRepository(information, entityManager);
    }
}
