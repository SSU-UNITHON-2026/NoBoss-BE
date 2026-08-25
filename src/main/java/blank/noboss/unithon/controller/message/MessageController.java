package blank.noboss.unithon.controller.message;

import blank.noboss.unithon.global.response.ApiResponse;
import blank.noboss.unithon.service.message.MessageService;
import blank.noboss.unithon.service.message.MessageApplyService;
import blank.noboss.unithon.service.message.dto.MessageApplyResponse;
import blank.noboss.unithon.service.message.dto.MessageRequest;
import blank.noboss.unithon.service.message.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Message")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;
    private final MessageApplyService messageApplyService;

    @Operation(summary = "AI 메시지 전송 및 제안 생성")
    @PostMapping
    public ApiResponse<MessageResponse> createMessage(
            @RequestBody(required = false) MessageRequest request
    ) {
        String userText = request == null ? null : request.text();
        return ApiResponse.success(messageService.createMessage(userText));
    }

    @Operation(summary = "AI 변경 제안 승인 및 적용")
    @PostMapping("/{messageId}/apply")
    public ApiResponse<MessageApplyResponse> applyMessage(
            @PathVariable Long messageId
    ) {
        return ApiResponse.success(messageApplyService.apply(messageId));
    }
}
