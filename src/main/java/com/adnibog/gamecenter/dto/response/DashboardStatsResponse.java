package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private int totalProjects;
    private Integer totalAdmins; // null if not super admin
    private List<GrowthData> projectGrowth;
}
