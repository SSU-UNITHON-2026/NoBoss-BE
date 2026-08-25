package blank.noboss.unithon.repository.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findFirstByProposalStatusOrderByCreatedAtDescIdDesc(ProposalStatus proposalStatus);
}
