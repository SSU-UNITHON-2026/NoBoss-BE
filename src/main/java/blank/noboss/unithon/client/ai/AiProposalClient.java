package blank.noboss.unithon.client.ai;

import blank.noboss.unithon.client.ai.dto.AiProposalRequest;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;

public interface AiProposalClient {

    AiProposalResponse generate(AiProposalRequest request);
}
