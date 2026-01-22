<template>
    <div class="container">
        <div class="up">
             <easy-player
                    :video-url="videoUrl"
                    live="true"
                    stretch="true"
                    autoplay>
            </easy-player>
        </div>
        <div class="down">
            <button  v-for="preset in presets" @click="toPreset(preset.presetId)">{{ preset.presetName }}</button>
        </div>

    </div>
</template>
<style lang='less' scoped src='./Home.less'></style>
<script src='./Home.js'></script>