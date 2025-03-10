package com.reqsync.Reqsync.Service;

import com.reqsync.Reqsync.CustomException.AlreadyUsedEmail;
import com.reqsync.Reqsync.CustomException.NoIdexist;
import com.reqsync.Reqsync.CustomException.UsersNotFound;
import com.reqsync.Reqsync.Dto.HelpRequestForRequestorDto;
import com.reqsync.Reqsync.Dto.HelpRequestFormDto;
import com.reqsync.Reqsync.Dto.RequestHelperIssueDto;
import com.reqsync.Reqsync.Entity.HelpRequest;
import com.reqsync.Reqsync.Entity.RequestHelperIssue;
import com.reqsync.Reqsync.Entity.RequestStatus;
import com.reqsync.Reqsync.Entity.Roles;
import com.reqsync.Reqsync.Entity.User;
import com.reqsync.Reqsync.Entity.Volunteer;
import com.reqsync.Reqsync.Events.HelpRequestCreatedEvent;
import com.reqsync.Reqsync.Repository.HelpRequestRepository;
import com.reqsync.Reqsync.Repository.HelpRequestorIssueRepository;
import com.reqsync.Reqsync.Repository.RoleRepository;
import com.reqsync.Reqsync.Repository.UserRepository;
import com.reqsync.Reqsync.Repository.VolunteerRepository;
import com.reqsync.Reqsync.Repository.VolunteerResolutionRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HelpRequestService {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VolunteerResolutionRepository volunteerResolutionRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HelpRequestorIssueRepository helpRequestorIssueRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // @Autowired
    // private NotificationService notificationService;

    @Transactional
    public void addHelpRequest(HelpRequestFormDto helpRequestFormDto) {
        // Check if the current user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated. Please log in first.");
        }

        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        log.debug("Here is the email  : " + userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        // Check if the user already has the "HELP_REQUESTER" role
        Roles helpRequesterRole = roleRepository.findByRole("HELPREQUESTER");
        Roles volunteerRoles = roleRepository.findByRole("VOLUNTEER");
        if (helpRequesterRole == null) {
            throw new IllegalArgumentException("Role not found: HELP_REQUESTER");
        }

        if (user.getRoles().contains(volunteerRoles)) {
            throw new AlreadyUsedEmail(
                    "You already has the HelpRequestor role or you are a volunteer if you are a volunterr then remove your role then ask for help");
        }

        if (!user.getRoles().contains(helpRequesterRole)) {
            // Add the "HELP_REQUESTER" role to the user
            user.getRoles().add(helpRequesterRole);
            userRepository.save(user); // Save the updated user with the new role
        }

        if (user.getName() == null || user.getPhone() == null || user.getArea() == null) {
            throw new IllegalArgumentException("User details are incomplete. Please update your profile first.");

        }

        // Convert HelpRequestDto to HelpRequest entity
        HelpRequest helpRequest = HelpRequest.builder()
                .user(user) // ✅ Set the requesting user
                .name(user.getName())
                .phone(user.getPhone())
                .area(user.getArea())
                .helpType(helpRequestFormDto.getHelpType())
                .message(helpRequestFormDto.getDescription())
                .build();

        // Save the help request to the database
        helpRequestRepository.save(helpRequest);
        eventPublisher.publishEvent(new HelpRequestCreatedEvent(this, helpRequest)); // ✅ Triggering event

    }

    @Transactional
    public boolean deleteHelpRequestorRole(String email) {
        Roles helpRequestorRoles = roleRepository.findByRole("HELPREQUESTER");
        if (helpRequestorRoles == null) {
            throw new IllegalArgumentException("Role not found: HELPREQUESTER");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsersNotFound("User not found with email: " + email));

        if (user != null) {
            if (!user.getRoles().contains(helpRequestorRoles)) {
                throw new IllegalArgumentException("User does not have the HELPREQUESTER role");
            }
            user.getRoles().remove(helpRequestorRoles);
            helpRequestRepository.deleteByUser(user);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean deleteHelpRequest(Long id) {
        if (helpRequestRepository.existsById(id)) {
            helpRequestRepository.deleteById(id);
            return true;
        }
        throw new NoIdexist("No id exist with this id");

    }

    // public boolean confirmRequestStatusByRequestor(Long id, boolean isResolved,
    // RequestHelperIssueDto hRequestHelperIssueDto) {
    // HelpRequest helpRequest = helpRequestRepository.findById(id)
    // .orElseThrow(() -> new IllegalArgumentException("Help request not found with
    // id: " + id));

    // Long volunteerId = volunteerResolutionRepository.findByHelpRequestId(id);

    // Volunteer volunteer = volunteerRepository.findById(volunteerId)
    // .orElseThrow(() -> new IllegalArgumentException("Volunteer not found with id:
    // " + volunteerId));

    // if (isResolved) {
    // deleteHelpRequest(id);
    // }
    // helpRequest.setStatus(RequestStatus.PENDING);
    // helpRequestRepository.save(helpRequest);
    // RequestHelperIssue requestHelperIssue = new RequestHelperIssue();
    // requestHelperIssue.setDescription(hRequestHelperIssueDto.getDescription());
    // requestHelperIssue.setVolunteerEmail(volunteer.getUser().getEmail());
    // helpRequestorIssueRepository.save(requestHelperIssue);
    // eventPublisher.publishEvent(new HelpRequestCreatedEvent(this, helpRequest));
    // return true;
    // }
}
