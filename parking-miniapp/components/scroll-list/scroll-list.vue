<template>
    <view class="scroll-list-wrap" :style="[scrollListWrapStyle]">
        <scroll-view
            class="scroll-view"
            :class="[elClass]"
            :style="[listWrapStyle]"
            scroll-y
            scroll-anchoring
            enable-back-to-top
            :scroll-top="scrollTop"
            :lower-threshold="defaultOption.lowerThreshold"
            @scroll="handleScroll"
            @touchend="handleTouchEnd"
            @touchmove.prevent.stop="handleTouchMove"
            @touchstart="handleTouchStart"
            @scrolltolower="handleScrolltolower"
        >
            <view class="scroll-content" :style="[scrollContentStyle]">
                <view class="pull-down-wrap">
                    <slot name="pulldown" v-if="$slots.pulldown"></slot>
                    <view class="refresh-view" :style="[refreshViewStyle]" v-else>
                        <view class="pull-down-animation" :class="{ refreshing: refreshing }" :style="[pullDownAnimationStyle]"></view>
                        <text class="pull-down-text" :style="[pullDownTextStyle]">{{ refreshStateText }}</text>
                    </view>
                </view>
                <view class="empty-wrap" v-if="showEmpty">
                    <slot name="empty" v-if="$slots.empty"></slot>
                    <view class="empty-view" v-else>
                        <image class="empty-image" :src="defaultOption.emptyImage || images.empty" mode="aspectFit"></image>
                        <text class="empty-text" :style="[emptyTextStyle]">{{ emptyText }}</text>
                    </view>
                </view>
                <view class="list-content"><slot></slot></view>
                <view class="pull-up-wrap" v-if="showPullUp">
                    <slot name="pullup" v-if="$slots.pullup"></slot>
                    <view class="load-view" v-else>
                        <view class="pull-up-animation" v-if="loading" :style="[pullUpAnimationStyle]"></view>
                        <text class="pull-up-text" :style="[pullUpTextStyle]">{{ loadStateText }}</text>
                    </view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script>
import images from './images.js';
export default {
    name: 'scroll-list',
    props: {
        option: {
            type: Object,
            default: () => ({})
        }
    },
    data() {
        return {
            defaultOption: {
                page: 1,
                size: 15,
                auto: false,
                height: null,
                disabled: false,
                background: '',
                emptyImage: '',
                offsetBottom: 0,
                pullDownSpeed: 0.5,
                lowerThreshold: 40,
                refresherThreshold: 80,
                refreshDelayed: 800,
                refreshFinishDelayed: 800,
                safeArea: false,
                emptyTextColor: '#82848a',
                loadTextColor: '#82848a',
                loadIconColor: '#82848a',
                refresherTextColor: '#82848a',
                refresherIconColor: '#82848a',
                emptyText: '暂无列表~',
                loadingText: '正在加载中~',
                loadFailText: '加载失败啦~',
                noMoreText: '没有更多啦~',
                refreshingText: '正在刷新~',
                refreshFailText: '刷新失败~',
                refreshSuccessText: '刷新成功~',
                pulldownText: '下拉刷新~',
                pulldownFinishText: '松开刷新~'
            },
            images,
            elClass: '',
            windowInfo: {},
            scrollTop: 0,
            scrollViewTop: -1,
            scrollViewHeight: 0,
            currentPage: 1,
            currentSize: 15,
            currentScrollTop: 0,
            emptyText: '暂无列表~',
            loadStateText: '正在加载中~',
            refreshStateText: '下拉刷新~',
            loadDisabled: false,
            loading: false,
            refreshing: false,
            refreshFinish: false,
            pulldowning: false,
            pullDownHeight: 0,
            showEmpty: false,
            showPullUp: false,
            showPullDown: false
        };
    },
    methods: {
        handleInit() {
            this.defaultOption = Object.assign(this.defaultOption, this.option);
            this.showEmpty = !this.defaultOption.auto;
            this.currentPage = this.defaultOption.page;
            this.currentSize = this.defaultOption.size;
            this.emptyText = this.defaultOption.emptyText;
            this.loadStateText = this.defaultOption.loadingText;
            this.refreshStateText = this.defaultOption.pulldownText;
            this.queryRect('.' + this.elClass).then(rect => {
                this.scrollViewTop = rect.top;
                if (this.defaultOption.auto) this.load();
            });
        },
        load() {
            if (this.defaultOption.disabled || this.loading || this.loadDisabled) return;
            this.loading = true;
            this.loadStateText = this.defaultOption.loadingText;
            this.showPullUp = true;
            let paging = { page: this.currentPage, size: this.currentSize };
            this.$emit('load', paging);
        },
        loadSuccess(data = {}) {
            const { list, total } = data;
            if (Array.isArray(list)) {
                if (list.length) {
                    if (list.length >= total) {
                        this.loadDisabled = true;
                        this.loadStateText = this.defaultOption.noMoreText;
                    } else {
                        this.loadDisabled = false;
                        this.currentPage++;
                        this.loadStateText = this.defaultOption.loadingText;
                        this.loadCompute();
                    }
                    this.showPullUp = true;
                    this.showEmpty = false;
                } else {
                    this.loadDisabled = true;
                    this.showPullUp = false;
                    this.showEmpty = true;
                }
                this.loading = false;
                this.$emit('loadSuccess', list);
            } else {
                this.loadFail();
                console.error('the list must be a array');
            }
        },
        loadFail() {
            this.loading = false;
            this.showEmpty = false;
            this.showPullUp = true;
            this.loadStateText = this.defaultOption.loadFailText;
            this.$emit('loadFail');
        },
        refresh() {
            if (this.pullDownHeight == this.defaultOption.refresherThreshold) {
                this.loading = false;
                this.showPullUp = false;
            } else {
                this.loading = true;
                this.showEmpty = false;
                this.showPullUp = true;
                this.loadStateText = this.defaultOption.refreshingText;
            }
            this.refreshFinish = false;
            this.refreshing = true;
            this.refreshStateText = this.defaultOption.refreshingText;
            this.currentPage = 1;
            this.currentSize = this.defaultOption.size;
            let paging = { page: this.currentPage, size: this.currentSize };
            setTimeout(() => {
                this.$emit('refresh', paging);
            }, this.defaultOption.refreshDelayed);
        },
        refreshSuccess(data) {
            const { list, total } = data;
            if (Array.isArray(list)) {
                if (list.length) {
                    if (list.length >= total) {
                        this.loadDisabled = true;
                        this.loadStateText = this.defaultOption.noMoreText;
                    } else {
                        this.currentPage++;
                        this.loadDisabled = false;
                        this.loadStateText = this.defaultOption.loadingText;
                        this.defaultOption.auto = false;
                        this.loadCompute();
                    }
                    this.showEmpty = false;
                    this.showPullUp = true;
                } else {
                    this.loadDisabled = true;
                    this.showPullUp = false;
                    this.showEmpty = true;
                    this.loadStateText = this.defaultOption.noMoreText;
                }
                this.loading = false;
                this.refreshStateText = this.defaultOption.refreshSuccessText;
                this.refreshing = false;
                this.pulldowning = false;
                this.$emit('refreshSuccess', list);
                setTimeout(() => {
                    this.refreshFinish = true;
                    this.pullDownHeight = 0;
                    this.showPullDown = false;
                    this.$emit('refreshSuccess');
                }, this.defaultOption.refreshFinishDelayed);
            } else {
                this.refreshFail();
                console.error('the list must be a array');
            }
        },
        refreshFail() {
            this.loadStateText = this.defaultOption.refreshFailText;
            this.refreshStateText = this.defaultOption.refreshFailText;
            this.loading = false;
            this.showPullUp = true;
            this.refreshing = false;
            this.pulldowning = false;
            setTimeout(() => {
                this.refreshFinish = true;
                this.pullDownHeight = 0;
                this.showPullDown = false;
                this.$emit('refreshError');
            }, this.defaultOption.refreshFinishDelayed);
        },
        loadCompute() {
            if (this.defaultOption.auto) {
                setTimeout(() => {
                    this.$nextTick(() => {
                        this.queryRect('.list-content').then(rect => {
                            if (rect.height <= this.scrollViewHeight) {
                                this.load();
                            }
                        });
                    });
                }, 100);
            }
        },
        handleScrolltolower(e) {
            if (this.loadDisabled) return;
            this.$emit('scrolltolower', e);
            this.load();
        },
        handleScroll(event) {
            this.currentScrollTop = event.detail.scrollTop;
            this.$emit('scroll', event.detail);
        },
        handleTouchStart(event) {
            if (this.defaultOption.disabled) return;
            this.currentTouchStartY = event.touches[0].clientY;
            this.$emit('touchStart', event);
        },
        handleTouchMove(event) {
            if (this.defaultOption.disabled || this.currentScrollTop) return;
            if (event.touches[0].clientY >= this.currentTouchStartY) {
                this.pulldowning = true;
                this.showPullDown = true;
                let pullDownDistance = (event.touches[0].clientY - this.currentTouchStartY) * this.defaultOption.pullDownSpeed;
                this.pullDownHeight = pullDownDistance > this.defaultOption.refresherThreshold ? this.defaultOption.refresherThreshold : pullDownDistance;
                this.refreshStateText =
                    this.pullDownHeight >= this.defaultOption.refresherThreshold ? this.defaultOption.pulldownFinishText : this.defaultOption.pulldownText;
                this.$emit('touchMove', event);
            }
        },
        handleTouchEnd(event) {
            if (this.defaultOption.disabled) return;
            if (this.pullDownHeight < this.defaultOption.refresherThreshold) {
                this.pulldowning = false;
                this.pullDownHeight = 0;
                this.showPullDown = false;
                this.$emit('refreshStop');
            } else {
                this.refresh();
            }
            this.$emit('touchEnd', event);
        },
        updateScrollView() {
            if (this.defaultOption.height) {
                this.scrollViewHeight = uni.upx2px(this.defaultOption.height);
            } else {
                this.scrollViewHeight = this.windowInfo.windowHeight - this.scrollViewTop;
            }
            this.scrollViewObserve();
        },
        listContentObserve() {
            this.disconnectObserve('_listContentObserve');
            const listContentObserve = this.createIntersectionObserver({
                thresholds: [0, 0.5, 1]
            });
            listContentObserve.relativeToViewport({
                top: -rect.top
            });
        },
        scrollViewObserve() {
            this.disconnectObserve('_scrollViewObserve');
            this.$nextTick(() => {
                this.queryRect('.' + this.elClass).then(rect => {
                    const scrollViewObserve = this.createIntersectionObserver({
                        thresholds: [0, 0.5, 1]
                    });
                    scrollViewObserve.relativeToViewport({
                        top: -rect.top
                    });
                    scrollViewObserve.observe('.' + this.elClass, position => {
                        this.scrollViewTop = position.boundingClientRect.top;
                    });
                    this._scrollViewObserve = scrollViewObserve;
                });
            });
        },
        disconnectObserve(observerName) {
            const observer = this[observerName];
            observer && observer.disconnect();
        },
        queryRect(selector, all) {
            return new Promise(resolve => {
                uni.createSelectorQuery()
                    .in(this)
                    [all ? 'selectAll' : 'select'](selector)
                    .boundingClientRect(rect => {
                        if (all && Array.isArray(rect) && rect.length) {
                            resolve(rect);
                        }
                        if (!all && rect) {
                            resolve(rect);
                        }
                    })
                    .exec();
            });
        },
        hexToRgb(hex) {
            const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
            hex = hex.replace(shorthandRegex, (m, r, g, b) => {
                return r + r + g + g + b + b;
            });
            const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
            return result
                ? {
                      r: parseInt(result[1], 16),
                      g: parseInt(result[2], 16),
                      b: parseInt(result[3], 16)
                  }
                : null;
        }
    },
    computed: {
        scrollListWrapStyle(){
            let style = {};
            style.background = this.defaultOption.background;
            return style;
        },
        listWrapStyle() {
            let style = {};
            const { offsetBottom } = this.defaultOption;
            style.height = this.scrollViewHeight - uni.upx2px(offsetBottom) + 'px';
            if (this.defaultOption.safeArea) style.paddingBottom = 'env(safe-area-inset-bottom) !important';
            return style;
        },
        scrollContentStyle() {
            const style = {};
            const { pullDownHeight, pulldowning, showPullDown } = this;
            style.transform = showPullDown ? `translateY(${pullDownHeight}px)` : `translateY(0px)`;
            style.transition = pulldowning ? `transform 100ms ease-out` : `transform 200ms cubic-bezier(0.19,1.64,0.42,0.72)`;
            return style;
        },
        refreshViewStyle() {
            const style = {};
            const { showPullDown } = this;
            style.opacity = showPullDown ? 1 : 0;
            return style;
        },
        pullDownAnimationStyle() {
            const style = {};
            const { refresherIconColor, refresherThreshold } = this.defaultOption;
            const { refreshing, pullDownHeight } = this;
            const { r, g, b } = this.hexToRgb(refresherIconColor);
            const rate = pullDownHeight / refresherThreshold;
            style.borderColor = `rgba(${r},${g},${b},0.2)`;
            style.borderTopColor = refresherIconColor;
            if (!refreshing) {
                style.transform = `rotate(${360 * rate}deg)`;
                style.transition = 'transform 100ms linear';
            }
            return style;
        },
        pullDownTextStyle() {
            const style = {};
            const { refresherTextColor } = this.defaultOption;
            style.color = refresherTextColor;
            return style;
        },
        pullUpAnimationStyle() {
            const style = {};
            const { loadIconColor } = this.defaultOption;
            const { r, g, b } = this.hexToRgb(loadIconColor);
            style.borderColor = `rgba(${r},${g},${b},0.2)`;
            style.borderTopColor = loadIconColor;
            return style;
        },
        pullUpTextStyle() {
            const style = {};
            const { loadTextColor } = this.defaultOption;
            style.color = loadTextColor;
            return style;
        },
        emptyTextStyle() {
            const style = {};
            const { emptyTextColor } = this.defaultOption;
            style.color = emptyTextColor;
            return style;
        }
    },
    watch: {
        scrollViewTop(val) {
            this.updateScrollView();
        }
    },
    created() {
        this.elClass = 'scroll-view-' + this._uid;
        this.windowInfo = uni.getSystemInfoSync();
    },
    mounted() {
        this.handleInit();
    }
};
</script>


<style scoped lang="scss">
.scroll-list-wrap {
    box-sizing: border-box;
    .scroll-view {
        position: relative;
        .scroll-content {
            height: 100%;
            display: flex;
            will-change: transform;
            flex-direction: column;
            .pull-down-wrap {
                left: 0;
                width: 100%;
                display: flex;
                padding: 30rpx 0;
                position: absolute;
                align-items: flex-end;
                justify-content: center;
                transform: translateY(-100%);
                .refresh-view {
                    display: flex;
                    justify-content: center;
                    .pull-down-animation {
                        width: 32rpx;
                        height: 32rpx;
                        border-width: 4rpx;
                        border-style: solid;
                        border-radius: 50%;
                        &.refreshing {
                            animation: spin 0.5s linear infinite;
                        }
                        @keyframes spin {
                            to {
                                transform: rotate(360deg);
                            }
                        }
                    }
                    .pull-down-text {
                        margin-left: 10rpx;
                    }
                }
            }
            .empty-wrap {
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                display: flex;
                position: absolute;
                align-items: center;
                flex-direction: column;
                .empty-view {
                    margin: auto;
                    display: flex;
                    align-items: center;
                    flex-direction: column;
                    .empty-image {
                        width: 200rpx;
                        height: 200rpx;
                    }
                    .empty-text {
                        color: #606266;
                        margin-top: 20rpx;
                    }
                }
            }
            .list-content {
            }
            .pull-up-wrap {
                display: flex;
                align-items: center;
                justify-content: center;
                .load-view {
                    padding: 20rpx 0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    .pull-up-animation {
                        width: 32rpx;
                        height: 32rpx;
                        border-width: 4rpx;
                        border-style: solid;
                        border-radius: 50%;
                        animation: spin 0.5s linear infinite;
                    }
                    .pull-up-text {
                        margin-left: 10rpx;
                    }
                }
            }
        }
    }
}
</style>
