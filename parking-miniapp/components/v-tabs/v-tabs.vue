<template>
    <view :id="elId" class="v-tabs">
        <view class="open-list" :style="{
          height,
          position: fixed ? 'fixed' : 'absolute'
        }"
            v-if="isUseOpenList" @click="handleShowList">
            <slot name="icon-unfold">
                <u-icon name="/static/images/index/ico_more.png" size="48" top="3"></u-icon>
            </slot>
        </view>
        <scroll-view id="scrollContainer" :scroll-x="scroll" :scroll-left="scroll ? scrollLeft : 0"
            :scroll-with-animation="scroll" :style="{ position: fixed ? 'fixed' : 'relative', zIndex: 1993  }">
            <view class="v-tabs__container" :style="{
          display: scroll ? 'inline-flex' : 'flex',
          whiteSpace: scroll ? 'nowrap' : 'normal',
          background: bgColor,
          height,
          padding
        }">
                <view class="v-tabs__container-item" v-for="(v, i) in tabs" :key="i" :style="{
            color: current == i ? activeColor : color,
            fontSize: current == i ? activeFontSize : fontSize,
            fontWeight: bold && current == i ? 'bold' : '',
            justifyContent: !scroll ? 'center' : '',
            flex: scroll ? '' : 1,
            padding: paddingItem
          }"
                    @click="change(i)">
                    {{ field ? v[field] : v }}
                </view>
                <view v-if="!pills" class="v-tabs__container-line" :style="{
            background: lineColor,
            width: lineWidth + 'px',
            height: lineHeight,
            borderRadius: lineRadius,
            left: lineLeft + 'px',
            transform: `translateX(-${lineWidth / 2}px)`
          }"></view>
                <view v-else class="v-tabs__container-pills" :style="{
            background: pillsColor,
            borderRadius: pillsBorderRadius,
            left: pillsLeft + 'px',
            width: currentWidth + 'px',
            height
          }"></view>
                <view v-if="isUseOpenList" style="width:62rpx;display: inline-block;"></view>
            </view>
        </scroll-view>
        <view class="v-tabs__placeholder" :style="{
        height: fixed ? height : '0',
        padding
      }"></view>
        <u-popup v-model="isShowList" mode="bottom" :mask="false" height="90%" negative-top="200" :closeable="true" >
            <text style="
            padding: 10px 15px;
    display: inline-block;
    font-size: 16px;
    font-weight: 500;
    text-align: left;
    color: #333333;">全部分类</text>
            <view class="flex u-flex-wrap" style="padding: 30rpx 0;">
                <view v-for="(item, index) in tabs" :key="index" :style="{
                  color: current == index ? activeColor : color,
                  fontSize: current == index ? fontSize : fontSize,
                  background: current == index ? 'rgba(192,25,31,0.06)' : '#f7f7f7',
                  padding: '2px 10px',
                  borderRadius: '19px',
                  border: current == index ?'1px solid #c0191f':'none',
                }"
                    class="scroll-view-item_wrap" :data-item="item" @click="change(index)">
                    {{ field ? item[field] : item }}
                </view>
            </view>
        </u-popup>
    </view>
</template>

<script>
    export default {
        props: {
            isUseOpenList: {
                type: Boolean,
                default: false
            },
            value: {
                type: Number,
                default: 0
            },
            tabs: {
                type: Array,
                default () {
                    return []
                }
            },
            bgColor: {
                type: String,
                default: '#fff'
            },
            padding: {
                type: String,
                default: '0'
            },
            color: {
                type: String,
                default: '#333'
            },
            activeColor: {
                type: String,
                default: '#2979ff'
            },
            fontSize: {
                type: String,
                default: '14px'
            },
            activeFontSize: {
                type: String,
                default: '14px'
            },
            bold: {
                type: Boolean,
                default: true
            },
            scroll: {
                type: Boolean,
                default: true
            },
            height: {
                type: String,
                default: '70rpx'
            },
            lineColor: {
                type: String,
                default: '#2979ff'
            },
            lineHeight: {
                type: String,
                default: '4rpx'
            },
            lineScale: {
                type: Number,
                default: 0.5
            },
            lineRadius: {
                type: String,
                default: '10rpx'
            },
            pills: {
                type: Boolean,
                deafult: false
            },
            pillsColor: {
                type: String,
                default: '#2979ff'
            },
            pillsBorderRadius: {
                type: String,
                default: '10rpx'
            },
            field: {
                type: String,
                default: ''
            },
            fixed: {
                type: Boolean,
                default: false
            },
            paddingItem: {
                type: String,
                default: '0 22rpx'
            }
        },
        data() {
            return {
                elId: '',
                lineWidth: 30,
                currentWidth: 0,
                lineLeft: 0,
                pillsLeft: 0,
                scrollLeft: 0,
                containerWidth: 0,
                current: 0,
                isShowList: false,
            }
        },
        watch: {
            value(newVal) {
                this.current = newVal
                this.$nextTick(() => {
                    this.getTabItemWidth()
                })
            },
            current(newVal) {
                this.$emit('input', newVal)
            },
            tabs(newVal) {
                this.$nextTick(() => {
                    this.getTabItemWidth()
                })
            }
        },
        methods: {
            handleShowList() {
                this.isShowList = !this.isShowList
            },
            randomString(len) {
                len = len || 32
                let $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678'
                let maxPos = $chars.length
                let pwd = ''
                for (let i = 0; i < len; i++) {
                    pwd += $chars.charAt(Math.floor(Math.random() * maxPos))
                }
                return pwd
            },
            change(index) {
                if (this.current !== index) {
                    if (this.isShowList) {
                        this.isShowList = false
                    }
                    this.current = index
                    this.$emit('change', index)
                }
            },
            getTabItemWidth() {
                let query = uni
                    .createSelectorQuery()
                    .in(this)
                query
                    .select(`#scrollContainer`)
                    .boundingClientRect((data) => {
                        if (!this.containerWidth && data) {
                            this.containerWidth = data.width
                        }
                    })
                    .exec()
                query
                    .selectAll('.v-tabs__container-item')
                    .boundingClientRect((data) => {
                        if (!data) {
                            return
                        }
                        let lineLeft = 0
                        let currentWidth = 0
                        if (data) {
                            for (let i = 0; i < data.length; i++) {
                                if (i < this.current) {
                                    lineLeft += data[i].width
                                } else if (i == this.current) {
                                    currentWidth = data[i].width
                                } else {
                                    break
                                }
                            }
                        }
                        this.currentWidth = currentWidth
                        this.lineWidth = currentWidth * this.lineScale * 1
                        this.lineLeft = lineLeft + currentWidth / 2
                        this.pillsLeft = lineLeft
                        if (this.scroll) {
                            this.scrollLeft = this.lineLeft - this.containerWidth / 2
                        }
                    })
                    .exec()
            }
        },
        mounted() {
            this.elId = 'xfjpeter_' + this.randomString()
            this.current = this.value
            this.$nextTick(() => {
                this.getTabItemWidth()
            })
        }
    }
</script>


<style lang="scss" scoped>
    .open-list {
        display: flex;
        justify-content: center;
        z-index: 1994;
        position: absolute;
        right: 0;
        height: 44px;
        width: 42px;
        text-align: center;
        font-weight: 600;
        color: #585858;
        background-color: #fff;
        box-shadow: -2px 0px 4px -2px rgba(0, 0, 0, 0.20);
    }

    .border-bottom-solid-ccc {
        border-bottom: 1rpx solid #ccc;
    }

    .text-lineheight-lg {
        line-height: 80rpx;
    }

    .size-28 {
        font-size: 28rpx;
    }

    .padding-bottom-sm {
        padding-bottom: 20rpx;
    }

    .scroll-view-item_wrap {
        min-width: 140rpx;
        text-align: center;
        line-height: 60rpx;
        font-size: 25rpx;
        border-radius: 32rpx;
        margin: 20rpx 0 0 30rpx;
    }

    .v-tabs {
        width: 100%;
        box-sizing: border-box;
        overflow: hidden;
        position: relative;

        ::-webkit-scrollbar {
            display: none;
        }

        &__container {
            min-width: 100%;
            position: relative;
            display: inline-flex;
            align-items: center;
            white-space: nowrap;
            overflow: hidden;

            &-item {
                display: flex;
                align-items: center;
                height: 100%;
                position: relative;
                z-index: 10;
                transition: all 0.3s;
                white-space: nowrap;
            }

            &-line {
                position: absolute;
                bottom: 0;
                transition: all 0.3s linear;
            }

            &-pills {
                position: absolute;
                transition: all 0.3s linear;
                z-index: 9;
            }
        }
    }
</style>
