package com.codepresso.codepresso.branch.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BranchSearchRequest {
    private String q;           // 검색어
    private Double lat;         // 위도
    private Double lng;         // 경도
    private Double radiusKm;    // 반경 (km)
    private int page;           // 페이지 번호
    private int size;           // 페이지 크기

    // 기본값 적용된 빌더
    public static BranchSearchRequest of(String q, Double lat, Double lng, Double radiusKm, Integer page, Integer size) {
        return BranchSearchRequest.builder()
                .q(q != null ? q.trim() : null)
                .lat(lat)
                .lng(lng)
                .radiusKm(radiusKm != null ? radiusKm : 2.0)  // 기본 반경 2km
                .page(page != null ? page : 0)
                .size(size != null && size > 0 ? size : 6)    // 기본 6개
                .build();
    }

    public boolean hasKeyword() {
        return q != null && !q.isBlank();
    }

    public boolean hasLocation() {
        return lat != null && lng != null;
    }
}
