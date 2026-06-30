package com.processmap.share.mapper;

import com.processmap.dto.ShareResponseDTO;
import com.processmap.share.entity.ShareLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring")
public abstract class ShareLinkMapper {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    protected String frontendUrl;

    @Mapping(target = "diagramId", source = "diagram.id")
    @Mapping(target = "shareUrl", expression = "java(frontendUrl + \"/share/\" + shareLink.getToken())")
    public abstract ShareResponseDTO toResponseDTO(ShareLink shareLink);
}
