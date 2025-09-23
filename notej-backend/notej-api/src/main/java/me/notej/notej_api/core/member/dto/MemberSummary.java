package me.notej.notej_api.core.member.dto;

import me.notej.notej_api.core.member.domain.Member;

public record MemberSummary(
        String memberUuid,
        String name,
        String nickname,
        String email,
        String avatarUrl
) {
    public static MemberSummary fromEntity(Member member) {
        return new MemberSummary(
                member.getMemberUuid(),
                member.getName(),
                member.getNickname(),
                member.getEmail(),
                member.getBlog().getProfileImage()
        );
    }
}
