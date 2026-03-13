package cn.iocoder.yudao.module.iot.service.security;

import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.ChannelRecordingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackClipReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackClipRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackQueryRecordingsReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackQueryRecordingsBatchReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackUrlReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackUrlRespVO;

import java.util.List;

public interface VideoPlaybackService {

    List<ChannelRecordingRespVO> queryRecordings(PlaybackQueryRecordingsReqVO reqVO);

    List<ChannelRecordingRespVO> queryRecordingsBatch(PlaybackQueryRecordingsBatchReqVO reqVO);

    PlaybackUrlRespVO getPlaybackUrl(PlaybackUrlReqVO reqVO);

    void closePlaybackStream(String streamId);

    PlaybackClipRespVO clipRecording(PlaybackClipReqVO reqVO);
}

