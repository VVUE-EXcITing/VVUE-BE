package com.exciting.vvue.notification.service;

import com.exciting.vvue.notification.exception.NotificationFailException;
import com.exciting.vvue.notification.exception.UserNotAddedToNotifyException;
import com.exciting.vvue.notification.model.Subscriber;
import com.exciting.vvue.notification.model.VvueNotification;
import com.exciting.vvue.notification.model.dto.NotReadNotificationDto;
import com.exciting.vvue.notification.model.dto.NotificationReqDto;
import com.exciting.vvue.notification.model.dto.VvueNotificationListDto;
import com.exciting.vvue.notification.model.dto.VvueNotificationResDto;
import com.exciting.vvue.notification.repository.SubscriberRepository;
import com.exciting.vvue.notification.repository.VvueNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final VvueNotificationRepository vvueNotificationRepository;
    private final SubscriberRepository subscriberRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    @Override
    public void subscribe(Long userId, String firebaseToken) {
        subscriberRepository.save(Subscriber.from(userId, firebaseToken));
    }

    @Override
    public boolean unsubscribe(Long userId, String firebaseToken) {
        Optional<Subscriber> subscriber = subscriberRepository.findByUserIdAndFirebaseToken(
            userId, firebaseToken);
        if (subscriber.isEmpty()) {
            return false;
        }
        subscriberRepository.delete(subscriber.get());
        return true;
    }

    @Override
    @Transactional
    public void sendByToken(NotificationReqDto notificationReqDto)
        throws UserNotAddedToNotifyException, NotificationFailException {
        log.debug("{sendByToken} " + notificationReqDto);
        Optional<Subscriber> notificationUser = subscriberRepository.findByUserId(
            notificationReqDto.getTargetUserId());
        log.debug("SENDBYTOKEN1{}" + notificationUser.isEmpty());
        if (!notificationUser.isPresent()) {
            return;
        }
        log.debug("SENDBYTOKEN2{}" + notificationUser.isEmpty());
//        String jsonString = null;
//        ObjectMapper objectMapper = new ObjectMapper();
//        if (notificationReqDto.getData() != null) {
//            try {
//                jsonString = objectMapper.writeValueAsString(notificationReqDto.getData());
//            } catch (IOException e) {
//                log.debug("!!!" + e.getMessage());
//                throw new NotificationFailException(e.getMessage());
//            }
//        }
        log.debug("[NOTIFY] 전");

        VvueNotification vvueNotification = VvueNotification.builder()
            .notificationType(notificationReqDto.getType())
            .content(notificationReqDto.getContent())
            .isRead(false)
            .receiverId(notificationReqDto.getTargetUserId())
            //.data(jsonString)
            .build();
        vvueNotificationRepository.save(vvueNotification);
        log.debug("[NOTIFY]" + vvueNotification);
        // title, body, image
        firebaseCloudMessageService.sendMessageTo(notificationUser.get().getFirebaseToken(),
            notificationReqDto.getContent().getTitle(),
            notificationReqDto.getContent().getBody());



//        Message message = Message.builder()
//            .putData("score","850")
//            .putData("time","2:45")
//            .setToken(notificationUser.get().getFirebaseToken())
////            .setNotification(Notification.builder()
////                .setTitle(notificationReqDto.getContent().getTitle())
////                .setBody(notificationReqDto.getContent().getBody())
////                .setImage(notificationReqDto.getContent().getImage())
////                .build())
//            //.putAllData(notificationReqDto.getData())// title, body, image
//            .build();
//        log.debug(">>>>>\t"+message.toString());
//        try {
//            log.debug("TRY");
//            log.debug(firebaseMessaging.toString());
//            String response = firebaseMessaging.send(message);
//            log.debug(response);
//
//        } catch (FirebaseMessagingException e) {
//            log.debug("FIREBASEMESSAGINGEXCEPTION"+e.getMessage());
//            log.debug(e.getCause().getMessage());
//            throw new NotificationFailException(
//                "알림 보내기를 실패하였습니다. target=" + notificationUser.get().getUserId());
//        }

    }

    @Override
    public VvueNotificationListDto getAllNotificationBy(Long userId, Long nextCursor,
        int size) {
        log.debug("[SIZE]>> " + vvueNotificationRepository.findByReceiverId(userId).size());
        List<VvueNotification> allNotifications =
            vvueNotificationRepository.findByReceiverId(userId);
        List<VvueNotificationResDto> res = allNotifications
            .stream()
            .filter(x -> x.getId() > nextCursor)
            .sorted(Comparator.comparing(VvueNotification::getCreatedAt).reversed())
            .limit(size)
            .map(VvueNotificationResDto::from)
            .toList();
        log.debug("[SIZE2]>> " + res.size());
        Optional<VvueNotificationResDto> maxValue = res.stream()
            .max(Comparator.comparing(VvueNotificationResDto::getId));
        Long lastCursorId = maxValue.isEmpty() ? -1 : maxValue.get().getId();
        long nextNotifyCnt = allNotifications
            .stream()
            .filter(x -> x.getId() > lastCursorId)
            .count();

        log.debug("[SIZE3] " + res.size());
        return VvueNotificationListDto.builder()
            .vvueNotificationResDtoList(res)
            .lastCursorId(lastCursorId)
            .hasNext(nextNotifyCnt == 0 ? false : true)
            .build();
    }

    @Override
    public NotReadNotificationDto getUnReadNotificationBy(Long userId) {
        int cnt = vvueNotificationRepository.countUnReadByReceiverId(userId);
        return new NotReadNotificationDto(cnt);
    }

    @Transactional
    @Override
    public void readAllUnReadNotify(Long userId) {
        vvueNotificationRepository.readAllUnReadNotify(userId);
    }

    @Transactional
    @Override
    public void readUnReadNotify(Long userId, Long notificationId) {
        vvueNotificationRepository.readUnReadNotify(userId, notificationId);

    }

}
