package com.yasarbilgi.ats.notification.listener;

import com.yasarbilgi.ats.department.repository.DepartmentManagerAssignmentRepository;
import com.yasarbilgi.ats.notification.event.ManagerReviewEnteredEvent;
import com.yasarbilgi.ats.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManagerReviewEmailNotificationListener {

    private final DepartmentManagerAssignmentRepository assignmentRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${notifications.manager-review.enabled:false}")
    private boolean enabled;

    @Value("${notifications.manager-review.from:no-reply@ats.local}")
    private String from;

    @Value("${notifications.manager-review.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendManagerReviewNotification(ManagerReviewEnteredEvent event) {
        if (!enabled) {
            log.debug("Manager review e-mail notification is disabled. processId={}",
                    event.candidateProcessId());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.error("Manager review e-mail is enabled but JavaMailSender is not configured. processId={}",
                    event.candidateProcessId());
            return;
        }

        var recipients = assignmentRepository
                .findAllByCompanyIdAndDepartmentIdAndActiveTrueAndEndedAtIsNullOrderByStartedAtDesc(
                        event.companyId(), event.departmentId())
                .stream()
                .map(assignment -> assignment.getUser())
                .filter(user -> user.isActive() && user.getStatus() == UserStatus.ACTIVE)
                .map(user -> user.getEmail())
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();

        if (recipients.isEmpty()) {
            log.warn("Manager review notification skipped: no active department manager. departmentId={}, processId={}",
                    event.departmentId(), event.candidateProcessId());
            return;
        }

        String candidateUrl = frontendBaseUrl.replaceAll("/$", "")
                + "/adaylar/" + event.candidateId();
        for (String recipient : recipients) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(recipient);
                message.setSubject("Yönetici değerlendirmesi bekleyen aday: " + event.candidateName());
                message.setText("Merhaba,\n\n"
                        + event.candidateName() + " adlı aday, " + event.positionTitle()
                        + " pozisyonu için Yönetici Değerlendirmesi aşamasına geçti.\n\n"
                        + "Departman: " + event.departmentName() + "\n"
                        + "Aday profili: " + candidateUrl + "\n\n"
                        + "Bu e-posta ATS tarafından otomatik gönderilmiştir.");
                mailSender.send(message);
                log.info("Manager review e-mail sent. processId={}, recipient={}",
                        event.candidateProcessId(), recipient);
            } catch (Exception exception) {
                log.error("Manager review e-mail could not be sent. processId={}, recipient={}",
                        event.candidateProcessId(), recipient, exception);
            }
        }
    }
}
