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

    Optional<Message> findFirstByProposalStatusOrderByCreatedAtDescIdDesc(ProposalStatus proposalStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select message from Message message where message.id = :id")
    Optional<Message> findByIdForUpdate(@Param("id") Long id);
}
