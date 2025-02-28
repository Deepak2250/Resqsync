package com.reqsync.Reqsync.Dto;

import java.util.List;
import com.reqsync.Reqsync.Dto.VolunterrTypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerDtoRoleAccess {
    private Long id;
    private String userEmail; // from Volunteer.user.email
    private String name;
    private String phone;
    private String area;
    private String about;
    private List<VolunterrTypes> volunteeringTypes;
    private List<String> skills;
    private boolean verified;
}
