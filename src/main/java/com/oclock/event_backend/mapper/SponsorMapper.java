package com.oclock.event_backend.mapper;

import com.oclock.event_backend.domain.Sponsor;
import com.oclock.event_backend.dto.SponsorDto;
import org.springframework.stereotype.Component;

@Component
public class SponsorMapper {
    public Sponsor toEntity(SponsorDto sponsorDto) {
        return Sponsor.builder()
                .id(sponsorDto.id())
                .name(sponsorDto.name())
                .description(sponsorDto.description())
                .logo(sponsorDto.logo())
                .build();
    }

    public SponsorDto toDto(Sponsor sponsor) {
        return SponsorDto.builder()
                .id(sponsor.getId())
                .name(sponsor.getName())
                .description(sponsor.getDescription())
                .logo(sponsor.getLogo())
                .build();
    }
}
