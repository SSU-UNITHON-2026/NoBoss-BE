package blank.noboss.unithon.repository.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findFirstByProjectIdAndProposalStatusOrderByCreatedAtDescIdDesc(
            Long projectId,
            ProposalStatus proposalStatus
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select message
            from Message message
            where message.id = :messageId
              and message.project.id = :projectId
            """)
    Optional<Message> findByIdAndProjectIdForUpdate(
            @Param("messageId") Long messageId,
            @Param("projectId") Long projectId
    );
}
