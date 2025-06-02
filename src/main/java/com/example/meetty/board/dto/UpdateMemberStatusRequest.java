package com.example.meetty.board.dto;

import com.example.meetty.board.entity.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberStatusRequest {
    @NotNull(message = "변경할 상태를 입력해주세요.")
    private MemberStatus status;
}
