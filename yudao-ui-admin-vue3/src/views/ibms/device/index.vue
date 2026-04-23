<template>
  <div class="ibms-device-page">
    <ContentWrap class="ibms-device-page__header glass-panel">
      <div class="left">
        <el-breadcrumb separator-icon="ArrowRight">
          <el-breadcrumb-item>IBMS平台</el-breadcrumb-item>
          <el-breadcrumb-item>设备管理</el-breadcrumb-item>
        </el-breadcrumb>
        <el-tag type="primary" size="small" class="ml-8px">V3.3 原型对齐中</el-tag>
      </div>
      <div class="right">
        <div class="status-pill">
          <span class="dot"></span>
          <span class="text">系统运行正常</span>
          <span class="sub">| 在线率 98.5%</span>
        </div>
        <el-button class="icon-btn" circle>
          <Icon icon="ep:bell" />
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="tabs">
        <el-button
          v-for="g in deviceTabs"
          :key="g.value"
          text
          :class="['tab-btn', { active: activeGroup === g.value }]"
          @click="onGroupTabChange(g.value)"
        >
          <Icon v-if="g.icon" :icon="g.icon" class="mr-4px" />
          {{ g.label }}
        </el-button>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="stats">
        <div class="stat-card">
          <div class="icon blue"><Icon icon="fa:server" /></div>
          <div class="info">
            <div class="num">{{ stats.total }}</div>
            <div class="label">全部设备</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon green"><Icon icon="fa:check-circle" /></div>
          <div class="info">
            <div class="num green-text">{{ stats.online }}</div>
            <div class="label">设备在线</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon indigo"><Icon icon="fa:map-pin" /></div>
          <div class="info">
            <div class="num indigo-text">{{ stats.pointsTotal }}</div>
            <div class="label">总通道</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon emerald"><Icon icon="fa:check-double" /></div>
          <div class="info">
            <div class="num emerald-text">{{ stats.pointsOnline }}</div>
            <div class="label">通道在线</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="icon amber"><Icon icon="fa:exclamation-triangle" /></div>
          <div class="info">
            <div class="num amber-text">{{ stats.pointsAlarm }}</div>
            <div class="label">通道告警</div>
          </div>
        </div>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <div class="toolbar">
        <div class="left">
          <el-input
            v-model="filters.keyword"
            class="w-360px"
            clearable
            placeholder="搜索设备名称、编码、通道..."
            @keyup.enter="resetPage"
          >
            <template #prefix><Icon icon="ep:search" /></template>
          </el-input>
          <el-button class="glass-btn" @click="advancedVisible = !advancedVisible">
            <Icon icon="ep:filter" class="mr-4px" /> 高级筛选
          </el-button>
        </div>
        <div class="right">
          <el-button v-if="selectedIds.length" class="glass-btn" @click="openBatchDialog">
            <Icon icon="ep:grid" class="mr-4px" /> 批量操作 ({{ selectedIds.length }})
          </el-button>
          <el-button type="primary" @click="openCreate">
            <Icon icon="ep:plus" class="mr-4px" /> 添加设备
          </el-button>
          <el-button type="primary" class="btn-purple" @click="openBatchCreate">
            <Icon icon="ep:plus" class="mr-4px" /> 批量添加
          </el-button>
          <el-button class="glass-btn" @click="exportDevices">
            <Icon icon="ep:download" />
          </el-button>
        </div>
      </div>

      <div class="rule-tip">
        <Icon icon="fa:tag" class="mr-6px text-blue" />
        编码规则(6段): 区域-子区域-系统码-设备类型-品牌码-序号
        例: F01-LBY-VI-CAM-HIK-001 | 序列号(sn): 设备添加后从设备自动获取
      </div>

      <div v-show="advancedVisible" class="advanced">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="8" :lg="5">
            <div class="field">
              <div class="label">专业分组</div>
              <el-select v-model="filters.group" clearable placeholder="全部" @change="onFilterGroupChange">
                <el-option
                  v-for="g in groupOptions"
                  :key="g.value"
                  :label="`${g.value} - ${g.label}`"
                  :value="String(g.value)"
                />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="5">
            <div class="field">
              <div class="label">系统</div>
              <el-select v-model="filters.system" clearable placeholder="全部系统">
                <el-option
                  v-for="s in filteredSystemOptionsForFilter"
                  :key="s.value"
                  :label="`${s.value} - ${s.label}`"
                  :value="String(s.value)"
                />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="5">
            <div class="field">
              <div class="label">设备类型</div>
              <el-select v-model="filters.deviceType" clearable placeholder="全部类型">
                <el-option v-for="t in deviceTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="5">
            <div class="field">
              <div class="label">品牌</div>
              <el-select v-model="filters.brand" clearable filterable placeholder="全部品牌">
                <el-option
                  v-for="b in brandOptions"
                  :key="b.value"
                  :label="`${b.value} - ${b.label}`"
                  :value="String(b.value)"
                />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="field">
              <div class="label">接入类型</div>
              <el-select v-model="filters.accessType" clearable placeholder="全部">
                <el-option v-for="a in accessTypeOptions" :key="a" :label="a" :value="a" />
              </el-select>
            </div>
          </el-col>
        </el-row>
      </div>
    </ContentWrap>

    <ContentWrap class="glass-panel">
      <el-table
        :data="pagedDevices"
        border
        style="width: 100%"
        @selection-change="onSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column label="设备信息 / 编码" min-width="260">
          <template #default="{ row }">
            <div class="cell-main">
              <div class="name">{{ row.name }}</div>
              <div class="code">{{ row.code }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="专业分组" prop="group" width="110" />
        <el-table-column label="系统" prop="system" width="90" />
        <el-table-column label="设备类型" prop="deviceType" width="120" />
        <el-table-column label="产品型号" prop="productModel" min-width="140" show-overflow-tooltip />
        <el-table-column label="品牌" prop="brand" width="120">
          <template #default="{ row }">
            {{ brandDisplay(row.brand) }}
          </template>
        </el-table-column>
        <el-table-column label="接入类型" prop="accessType" width="120" />
        <el-table-column label="IP地址" prop="ip" width="140" />
        <el-table-column label="接入协议" prop="protocol" width="120" />
        <el-table-column label="序列号" prop="sn" min-width="160" show-overflow-tooltip />
        <el-table-column label="ProductKey" prop="productKey" min-width="180" show-overflow-tooltip />
        <el-table-column label="通道数" prop="pointCount" width="90" />
        <el-table-column label="空间位置" prop="space" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'online'" type="success" effect="dark">在线</el-tag>
            <el-tag v-else-if="row.status === 'offline'" type="info" effect="dark">离线</el-tag>
            <el-tag v-else type="warning" effect="dark">告警</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="total">显示 {{ pageStart }}-{{ pageEnd }} 共 {{ filteredDevices.length }}</div>
        <el-pagination
          v-model:current-page="page.pageNo"
          v-model:page-size="page.pageSize"
          :total="filteredDevices.length"
          layout="prev, pager, next, sizes"
          @current-change="handlePageChange"
          @size-change="resetPage"
        />
      </div>
    </ContentWrap>

    <div v-if="selectedIds.length" class="batch-bar glass-panel">
      <div class="left">
        已选择 <span class="count">{{ selectedIds.length }}</span> 个设备
        <el-button link class="clear" @click="clearSelection">清除选择</el-button>
      </div>
      <div class="right">
        <el-button class="glass-btn" @click="batchAction('move')">
          <Icon icon="ep:location" class="mr-4px" /> 批量移动
        </el-button>
        <el-button class="glass-btn" @click="batchAction('status')">
          <Icon icon="ep:switch" class="mr-4px" /> 状态变更
        </el-button>
        <el-button class="glass-btn" @click="batchAction('export')">
          <Icon icon="ep:document" class="mr-4px" /> 导出
        </el-button>
        <el-button class="danger-btn" @click="batchAction('delete')">
          <Icon icon="ep:delete" class="mr-4px" /> 删除
        </el-button>
      </div>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑设备' : '添加新设备'" width="920px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="专业分组" prop="group">
              <el-select v-model="form.group" placeholder="请选择" @change="onFormGroupChange">
                <el-option
                  v-for="g in groupOptions"
                  :key="g.value"
                  :label="`${g.value} - ${g.label}`"
                  :value="String(g.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统" prop="system">
              <el-select v-model="form.system" placeholder="请选择" @change="onFormSystemChange">
                <el-option
                  v-for="s in filteredSystemOptionsForForm"
                  :key="s.value"
                  :label="`${s.value} - ${s.label}`"
                  :value="String(s.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择">
                <el-option v-for="t in deviceTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-select v-model="form.brand" filterable placeholder="请选择品牌码（ibms_brand）">
                <el-option
                  v-for="b in brandOptions"
                  :key="b.value"
                  :label="`${b.value} - ${b.label}`"
                  :value="String(b.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品型号">
              <el-select
                v-model="form.productModel"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="输入或选择产品型号"
                :loading="productModelLoading"
                @visible-change="onProductModelVisibleChange"
              >
                <el-option v-for="m in productModelOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接入类型" prop="accessType">
              <el-select v-model="form.accessType" placeholder="请选择">
                <el-option v-for="a in accessTypeOptions" :key="a" :label="a" :value="a" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序号(3位)" prop="seq">
              <el-input v-model="form.seq" maxlength="3" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">点位类型预览（随产品型号联动）</el-divider>
        <div v-if="templateLoading" class="extra-tip">正在加载点位类型模板…</div>
        <template v-else-if="templateProduct?.pointTypes?.length">
          <div class="point-preview">
            <div class="point-preview__head">
              预计生成通道总数：<b>{{ templatePointTotal }}</b>
            </div>
            <div class="point-preview__list">
              <el-tag
                v-for="pt in templateProduct.pointTypes"
                :key="`${pt.pointTypeCode}-${pt.name}-${pt.count}`"
                type="primary"
                effect="plain"
              >
                {{ pt.pointTypeCode }}{{ pt.name ? ` - ${pt.name}` : '' }} x {{ pt.count || 0 }}
              </el-tag>
            </div>
          </div>
        </template>
        <div v-else class="extra-tip">
          {{
            templateProduct
              ? '已匹配产品，但该产品未配置点位类型；保存后不会自动生成通道。'
              : '未匹配到产品模板，无法预览点位类型。'
          }}
        </div>

        <!-- 通道：紧挨点位预览下方，标题行右侧「获取通道」 -->
        <div class="channel-block">
          <div class="channel-block__head">
            <div class="channel-block__title-row">
              <span class="channel-block__title">通道列表</span>
              <span v-if="channelList.length" class="channel-count">（{{ channelList.length }}）</span>
              <span v-else-if="!form.id" class="channel-hint">请先保存设备，再同步设备侧通道</span>
            </div>
            <el-button
              type="primary"
              size="small"
              :loading="channelSyncing"
              :disabled="channelLoading || !form.id"
              @click="syncChannels"
            >
              <Icon icon="ep:refresh" class="mr-4px" />
              获取通道
            </el-button>
          </div>
          <div v-if="channelSyncing" class="extra-tip channel-tip-inline">正在从设备同步通道，请稍候…</div>
          <div v-if="channelLoading" class="extra-tip">正在加载通道列表…</div>
          <template v-else-if="channelList.length">
            <el-table
              :data="channelList"
              size="small"
              border
              stripe
              max-height="240"
              class="channel-table"
            >
              <el-table-column prop="channelNo" label="通道号" width="70" align="center" />
              <el-table-column prop="name" label="通道名称" min-width="120" show-overflow-tooltip />
              <el-table-column prop="ip" label="IP 地址" width="120" show-overflow-tooltip />
              <el-table-column label="状态" width="72" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="row.status === 'online' ? 'success' : row.status === 'warning' ? 'warning' : 'info'"
                    size="small"
                    effect="plain"
                  >
                    {{ row.status === 'online' ? '在线' : row.status === 'warning' ? '告警' : '离线' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="typeCode" label="类型" width="56" align="center" />
              <el-table-column prop="dataSource" label="数据源" width="72" align="center" />
            </el-table>
          </template>
          <div v-else-if="form.id" class="extra-tip channel-empty">暂无通道，点击「获取通道」从 NVR 同步。</div>
        </div>

        <el-divider content-position="left">扩展属性（随产品型号联动）</el-divider>

        <!-- 接入参数：按接入类型展示表单，保存时合并写入 extra，无需手写 JSON -->
        <el-card shadow="never" class="access-params-card">
          <template #header>
            <span>接入参数</span>
            <span class="access-params-card__sub"
              >（随「接入类型」切换；IP 接入时随「接入协议」展示不同参数，与下方产品扩展字段一并保存为 extra）</span
            >
          </template>

          <template v-if="form.accessType === 'IP'">
            <div class="extra-tip access-params-tip">
              <template v-if="isIpProtocolMqtt">
                MQTT：填写 Broker 地址/端口、Client ID、主题等；认证使用下方用户名/密码（写入 extra）。
              </template>
              <template v-else-if="isIpProtocolModbusTcp">
                Modbus TCP：设备 IP、TCP 端口（常见 502）与从站地址（Unit ID）。
              </template>
              <template v-else-if="isIpProtocolBacnet">
                BACnet/IP：设备 IP、UDP 端口（常见 47808）与设备实例号。
              </template>
              <template v-else-if="isIpProtocolVideo">
                ONVIF / GB28181 等：设备 IP、HTTP/RTSP/SDK（TCP）端口与登录凭据。
              </template>
              <template v-else>
                请先选择「接入协议」以展示对应参数；未选择时显示通用端口项便于兼容旧数据。RS485 / 韦根 / 无线等非 IP
                接入无设备 IP 字段。
              </template>
            </div>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item :label="ipHostLabel">
                  <el-input v-model="form.ip" :placeholder="ipHostPlaceholder" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接入协议">
                  <el-select v-model="form.protocol" placeholder="请选择" class="w-full">
                    <el-option v-for="p in protocolOptions" :key="p" :label="p" :value="p" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- ONVIF / GB28181 -->
            <el-row v-if="isIpProtocolVideo" :gutter="12">
              <el-col :span="8">
                <el-form-item label="TCP 端口">
                  <el-input v-model="accessIp.tcpPort" placeholder="如 SDK 37777" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="HTTP 端口">
                  <el-input v-model="accessIp.httpPort" placeholder="如 80" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="RTSP 端口">
                  <el-input v-model="accessIp.rtspPort" placeholder="如 554" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="登录用户名">
                  <el-input v-model="accessIp.username" placeholder="可选" clearable autocomplete="off" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="登录密码">
                  <el-input
                    v-model="accessIp.password"
                    type="password"
                    show-password
                    placeholder="可选"
                    clearable
                    autocomplete="new-password"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- Modbus TCP -->
            <el-row v-if="isIpProtocolModbusTcp" :gutter="12">
              <el-col :span="8">
                <el-form-item label="TCP 端口">
                  <el-input v-model="accessIp.tcpPort" placeholder="如 502" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="从站地址">
                  <el-input v-model="accessModbusTcp.unitId" placeholder="Unit ID，如 1" clearable />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- BACnet/IP -->
            <el-row v-if="isIpProtocolBacnet" :gutter="12">
              <el-col :span="8">
                <el-form-item label="BACnet 端口">
                  <el-input v-model="accessBacnet.udpPort" placeholder="UDP，如 47808" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="设备实例号">
                  <el-input v-model="accessBacnet.deviceInstance" placeholder="Device Instance" clearable />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- MQTT -->
            <el-row v-if="isIpProtocolMqtt" :gutter="12">
              <el-col :span="8">
                <el-form-item label="Broker 端口">
                  <el-input v-model="accessIpMqtt.port" placeholder="如 1883 / 8883" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="16">
                <el-form-item label="Client ID">
                  <el-input v-model="accessIpMqtt.clientId" placeholder="可选，留空可由网关/平台生成" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="订阅主题">
                  <el-input v-model="accessIpMqtt.subscribeTopic" placeholder="如 device/+/telemetry" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="发布主题">
                  <el-input v-model="accessIpMqtt.publishTopic" placeholder="可选" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="TLS">
                  <el-checkbox v-model="accessIpMqtt.useTls">启用 TLS（MQTTS，常见端口 8883）</el-checkbox>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="accessIp.username" placeholder="Broker 认证，可选" clearable autocomplete="off" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="密码">
                  <el-input
                    v-model="accessIp.password"
                    type="password"
                    show-password
                    placeholder="可选"
                    clearable
                    autocomplete="new-password"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 未选协议：兼容旧数据，保留原「全套」端口项 -->
            <el-row v-if="isIpProtocolGeneric" :gutter="12">
              <el-col :span="8">
                <el-form-item label="TCP 端口">
                  <el-input v-model="accessIp.tcpPort" placeholder="如 37777" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="HTTP 端口">
                  <el-input v-model="accessIp.httpPort" placeholder="如 80" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="RTSP 端口">
                  <el-input v-model="accessIp.rtspPort" placeholder="如 554" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="登录用户名">
                  <el-input v-model="accessIp.username" placeholder="可选" clearable autocomplete="off" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="登录密码">
                  <el-input
                    v-model="accessIp.password"
                    type="password"
                    show-password
                    placeholder="可选"
                    clearable
                    autocomplete="new-password"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <template v-else-if="form.accessType === 'RS485'">
            <div class="extra-tip access-params-tip">适用于 Modbus RTU 等串口接入；保存后写入 extra。</div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="串口">
                  <el-input v-model="accessRs485.serialPort" placeholder="如 COM3 / /dev/ttyUSB0" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="波特率">
                  <el-select v-model="accessRs485.baudRate" placeholder="请选择" class="w-full">
                    <el-option label="9600" value="9600" />
                    <el-option label="19200" value="19200" />
                    <el-option label="38400" value="38400" />
                    <el-option label="57600" value="57600" />
                    <el-option label="115200" value="115200" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="从站地址">
                  <el-input v-model="accessRs485.slaveId" placeholder="如 1" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="数据位">
                  <el-select v-model="accessRs485.dataBits" class="w-full">
                    <el-option label="8" value="8" />
                    <el-option label="7" value="7" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="停止位">
                  <el-select v-model="accessRs485.stopBits" class="w-full">
                    <el-option label="1" value="1" />
                    <el-option label="2" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="校验">
                  <el-select v-model="accessRs485.parity" class="w-full">
                    <el-option label="无 (N)" value="N" />
                    <el-option label="偶 (E)" value="E" />
                    <el-option label="奇 (O)" value="O" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 韦根 -->
          <template v-else-if="form.accessType === '韦根'">
            <div class="extra-tip access-params-tip">
              韦根读头/门禁常用参数；保存后写入 extra（键名 wiegand*），供门禁控制器或网关解析。
            </div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="韦根格式">
                  <el-select v-model="accessWiegand.format" class="w-full" placeholder="请选择">
                    <el-option label="26 位" value="26" />
                    <el-option label="34 位" value="34" />
                    <el-option label="37 位" value="37" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="进出方向">
                  <el-select v-model="accessWiegand.direction" class="w-full">
                    <el-option label="进" value="IN" />
                    <el-option label="出" value="OUT" />
                    <el-option label="双向" value="BOTH" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="读头号">
                  <el-input v-model="accessWiegand.readerNo" placeholder="如 1、A1" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="控制器门点">
                  <el-input v-model="accessWiegand.doorIndex" placeholder="门禁控制器上的门序号/通道号" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label=" ">
                  <el-checkbox v-model="accessWiegand.invert">数据位取反（部分读头需勾选）</el-checkbox>
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 无线 -->
          <template v-else-if="form.accessType === '无线'">
            <div class="extra-tip access-params-tip">
              LoRa / ZigBee / WiFi 等无线接入标识与网络参数；保存后写入 extra（键名 wireless*）。
            </div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="无线协议">
                  <el-select v-model="accessWireless.protocol" class="w-full" clearable placeholder="请选择">
                    <el-option label="LoRa" value="LoRa" />
                    <el-option label="ZigBee" value="ZigBee" />
                    <el-option label="WiFi" value="WiFi" />
                    <el-option label="Sub-1G" value="Sub1G" />
                    <el-option label="NB-IoT" value="NB-IoT" />
                    <el-option label="4G / Cat.1" value="4G" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="设备 EUI / MAC">
                  <el-input v-model="accessWireless.deviceEui" placeholder="如 8 字节十六进制" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="网关 / 协调器 ID">
                  <el-input v-model="accessWireless.gatewayId" placeholder="可选" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="PAN ID">
                  <el-input v-model="accessWireless.panId" placeholder="ZigBee 等" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="射频信道">
                  <el-input v-model="accessWireless.rfChannel" placeholder="信道号或频点说明" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="WiFi SSID">
                  <el-input v-model="accessWireless.ssid" placeholder="仅 WiFi 时填写" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="应用密钥 / PSK">
                  <el-input
                    v-model="accessWireless.appKey"
                    type="password"
                    show-password
                    placeholder="LoRa AppKey 或 WiFi 密码等，可选"
                    clearable
                    autocomplete="new-password"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 模拟量 -->
          <template v-else-if="form.accessType === '模拟量'">
            <div class="extra-tip access-params-tip">
              4–20mA、电压、热阻等模拟输入量程与工程单位；保存后写入 extra（键名 analog*）。
            </div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="信号类型">
                  <el-select v-model="accessAnalog.signalType" class="w-full">
                    <el-option label="4–20mA" value="4-20mA" />
                    <el-option label="0–10V" value="0-10V" />
                    <el-option label="0–5V" value="0-5V" />
                    <el-option label="PT100" value="PT100" />
                    <el-option label="NTC" value="NTC" />
                    <el-option label="其它" value="Other" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="物理通道号">
                  <el-input v-model="accessAnalog.inputNo" placeholder="模块 AI 通道，如 AI1" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="工程单位">
                  <el-input v-model="accessAnalog.unit" placeholder="如 ℃、kPa、%" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="量程下限">
                  <el-input v-model="accessAnalog.rangeMin" placeholder="工程值下限" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="量程上限">
                  <el-input v-model="accessAnalog.rangeMax" placeholder="工程值上限" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="ADC 分辨率">
                  <el-select v-model="accessAnalog.adcBits" class="w-full">
                    <el-option label="12 位" value="12" />
                    <el-option label="16 位" value="16" />
                    <el-option label="24 位" value="24" />
                    <el-option label="其它 / 未知" value="auto" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 开关量 -->
          <template v-else-if="form.accessType === '开关量'">
            <div class="extra-tip access-params-tip">
              干接点 / DI 等数字输入；保存后写入 extra（键名 dio*）。
            </div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="输入编号">
                  <el-input v-model="accessDigital.inputNo" placeholder="如 DI3、IN-01" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="触点类型">
                  <el-select v-model="accessDigital.contactType" class="w-full">
                    <el-option label="常开 NO" value="NO" />
                    <el-option label="常闭 NC" value="NC" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="有效电平">
                  <el-select v-model="accessDigital.activeLevel" class="w-full">
                    <el-option label="高电平有效" value="HIGH" />
                    <el-option label="低电平有效" value="LOW" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="消抖 (ms)">
                  <el-input v-model="accessDigital.debounceMs" placeholder="如 20" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="轮询周期 (ms)">
                  <el-input v-model="accessDigital.pollingMs" placeholder="可选，轮询采集时填写" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="模块地址">
                  <el-input v-model="accessDigital.moduleAddress" placeholder="Modbus 站号等，可选" clearable />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <template v-else>
            <div class="extra-tip access-params-tip">
              请先选择「接入类型」；若列表新增类型，可先用下方「高级：手动编辑 JSON」补充参数。
            </div>
          </template>
        </el-card>

        <div v-if="templateLoading" class="extra-tip">正在加载产品扩展模板…</div>
        <template v-else-if="templateProduct?.properties?.length">
          <div v-if="templateProduct" class="extra-head">
            匹配产品：<b>{{ templateProduct.productName }}</b>
            <span class="muted">（{{ templateProduct.modelNumber }} · {{ templateProduct.productCode }}）</span>
          </div>
          <el-row :gutter="12">
            <el-col v-for="prop in templateProduct?.properties || []" :key="prop.propName" :span="12">
              <el-form-item :label="propertyLabel(prop)">
                <el-select
                  v-if="prop.type === 'select'"
                  v-model="extraValues[prop.propName]"
                  class="w-full"
                  clearable
                  placeholder="请选择"
                >
                  <el-option v-for="opt in propertySelectOptions(prop)" :key="opt" :label="opt" :value="opt" />
                </el-select>
                <el-input
                  v-else-if="prop.type === 'number'"
                  v-model="extraValues[prop.propName]"
                  placeholder="请输入数字"
                />
                <el-checkbox
                  v-else-if="prop.type === 'checkbox'"
                  :model-value="extraValues[prop.propName] === 'true'"
                  @update:model-value="(v: boolean) => (extraValues[prop.propName] = v ? 'true' : 'false')"
                >
                  启用
                </el-checkbox>
                <el-input v-else v-model="extraValues[prop.propName]" type="text" placeholder="请输入" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        <div v-else class="extra-fallback">
          <div class="extra-tip">
            {{
              templateProduct && !templateProduct.properties?.length
                ? '已匹配产品，但尚未配置扩展属性定义；可在产品管理中补充属性模板。'
                : '未匹配到 IBMS 产品库中的型号，扩展配置可用手动 JSON（与库中产品型号完全一致时将自动加载表单）。'
            }}
          </div>
        </div>

        <el-collapse v-model="extraAdvancedCollapse" class="extra-advanced-collapse">
          <el-collapse-item title="高级：手动编辑 extra JSON（可选，与接入参数、产品字段合并保存）" name="json">
            <el-form-item label="扩展 JSON" label-width="100px">
              <el-input
                v-model="extraManualJson"
                type="textarea"
                :rows="5"
                placeholder='例如 {"resolution":"1920x1080"}；留空则仅保存接入参数与产品扩展字段'
              />
            </el-form-item>
          </el-collapse-item>
        </el-collapse>

        <el-divider />

        <el-form-item label="ProductKey">
          <el-input
            v-model="form.productKey"
            readonly
            :placeholder="dialog.isEdit ? '' : '保存后由系统自动生成'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@/components/Icon'
import { DICT_TYPE, getDictLabel, getStrDictOptions, parseDictRemark } from '@/utils/dict'
import * as IbmsDeviceApi from '@/api/iot/ibms/device'
import * as IbmsProductApi from '@/api/iot/ibms/product'
import type { IbmsProductPropertyVO, IbmsProductRespVO } from '@/api/iot/ibms/product'
import * as IbmsChannelApi from '@/api/iot/ibms/channel'
import type { IbmsChannelRespVO } from '@/api/iot/ibms/channel'

defineOptions({ name: 'IbmsDevice' })

type DeviceStatus = 'online' | 'offline' | 'warning'
type DeviceRow = {
  id: number
  name: string
  code: string
  group: string
  system: string
  deviceType: string
  productModel: string
  brand: string
  accessType: string
  ip?: string
  protocol?: string
  sn?: string
  productKey?: string
  extra?: string
  pointCount: number
  pointsOnline: number
  pointsAlarm: number
  space?: string
  status: DeviceStatus
}

const groupOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_GROUP))
const systemOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_SYSTEM))
const deviceTypeOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_DEVICE_TYPE).map((d) => d.value as string))
const brandOptions = computed(() => getStrDictOptions(DICT_TYPE.IBMS_BRAND))

/** 表格展示：存库为品牌码；无字典项时原样显示（兼容历史） */
const brandDisplay = (code?: string) => {
  if (!code) return ''
  return getDictLabel(DICT_TYPE.IBMS_BRAND, code) || code
}
const accessTypeOptions = ['IP', 'RS485', '韦根', '无线', '模拟量', '开关量']
const protocolOptions = ['ONVIF', 'GB28181', 'Modbus TCP', 'BACnet', 'MQTT']

const deviceTabs = [
  { value: 'all', label: '全部设备', icon: '' },
  { value: 'SA', label: '智慧安防 SA', icon: 'fa:shield-alt' },
  { value: 'ST', label: '智慧通行 ST', icon: 'fa:door-open' },
  { value: 'SB', label: '智慧建筑 SB', icon: 'fa:building' },
  { value: 'SE', label: '智慧能源 SE', icon: 'fa:bolt' },
  { value: 'SF', label: '消防安全 SF', icon: 'fa:fire-extinguisher' },
  { value: 'GW', label: '协议网关 GW', icon: 'fa:globe' }
]

const activeGroup = ref<'all' | string>('all')
const advancedVisible = ref(false)
const devices = ref<IbmsDeviceApi.IbmsDeviceRespVO[]>([])

const filters = reactive({
  keyword: '',
  group: '' as string | undefined,
  system: '' as string | undefined,
  deviceType: '' as string | undefined,
  brand: '' as string | undefined,
  accessType: '' as string | undefined
})

const filteredSystemOptionsForFilter = computed(() => {
  if (!filters.group) return systemOptions.value
  return systemOptions.value.filter((s) => {
    const rm = parseDictRemark<{ group?: string }>(s.remark)
    return !rm?.group || rm.group === filters.group
  })
})

const onFilterGroupChange = () => {
  filters.system = ''
}

const page = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

const loading = ref(false)

const onGroupTabChange = (val: string) => {
  activeGroup.value = val
  resetPage()
}

const pagedDevices = computed<DeviceRow[]>(() => {
  return devices.value.map((d) => ({
    id: d.id,
    name: d.name,
    code: d.deviceCode,
    group: d.groupCode,
    system: d.systemCode,
    deviceType: d.deviceTypeCode,
    productModel: d.productModel,
    brand: d.brand,
    accessType: d.accessType,
    ip: d.ip,
    protocol: d.protocol,
    sn: d.sn,
    productKey: d.productKey,
    extra: d.extra,
    pointCount: d.pointCount ?? 0,
    pointsOnline: d.pointsOnline ?? 0,
    pointsAlarm: d.pointsAlarm ?? 0,
    space: d.space,
    status: (d.pointsAlarm ?? 0) > 0 ? 'warning' : 'online'
  }))
})

// 表格 footer 使用了 filteredDevices，这里补齐为当前分页后的展示数据
// （后续如果要做“前端二次过滤”，可以把 pagedDevices 的逻辑拆到 filteredDevices 再分页）
const filteredDevices = computed<DeviceRow[]>(() => pagedDevices.value)

const pageStart = computed(() => (page.total ? (page.pageNo - 1) * page.pageSize + 1 : 0))
const pageEnd = computed(() => Math.min(page.total, (page.pageNo - 1) * page.pageSize + pagedDevices.value.length))

const stats = computed(() => {
  const list = pagedDevices.value
  return {
    total: page.total,
    online: list.filter((d) => d.status === 'online').length,
    pointsTotal: list.reduce((sum, d) => sum + d.pointCount, 0),
    pointsOnline: list.reduce((sum, d) => sum + d.pointsOnline, 0),
    pointsAlarm: list.reduce((sum, d) => sum + d.pointsAlarm, 0)
  }
})

const selectedIds = ref<number[]>([])
const onSelectionChange = (rows: DeviceRow[]) => {
  selectedIds.value = rows.map((r) => r.id)
}

const clearSelection = () => {
  selectedIds.value = []
}

const resetPage = () => {
  page.pageNo = 1
  fetchPage()
}

const handlePageChange = (no: number) => {
  page.pageNo = no
  fetchPage()
}

const fetchPage = async () => {
  loading.value = true
  try {
    const res = await IbmsDeviceApi.getDevicePage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      groupCode: activeGroup.value !== 'all' ? (activeGroup.value as string) : filters.group || undefined,
      systemCode: filters.system || undefined,
      deviceTypeCode: filters.deviceType || undefined,
      brand: filters.brand || undefined,
      accessType: filters.accessType || undefined
    })
    devices.value = res.list || []
    page.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const exportDevices = () => {
  IbmsDeviceApi.exportDeviceExcel({
    pageNo: 1,
    pageSize: -1,
    keyword: filters.keyword || undefined,
    groupCode: activeGroup.value !== 'all' ? (activeGroup.value as string) : filters.group || undefined,
    systemCode: filters.system || undefined,
    deviceTypeCode: filters.deviceType || undefined,
    brand: filters.brand || undefined,
    accessType: filters.accessType || undefined
  })
}

const openBatchDialog = () => {
  ElMessage.info('批量操作 UI 已就绪，接口待接入')
}

const openBatchCreate = () => {
  ElMessage.info('批量添加向导 UI 已就绪，接口待接入')
}

const batchAction = (type: 'move' | 'status' | 'export' | 'delete') => {
  if (!selectedIds.value.length) return
  if (type === 'export') {
    const list = devices.value.filter((d) => selectedIds.value.includes(d.id))
    const blob = new Blob([JSON.stringify(list, null, 2)], { type: 'application/json;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'ibms-devices-selected.json'
    a.click()
    URL.revokeObjectURL(url)
    return
  }
  if (type === 'delete') {
    ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个设备？`, '提示', { type: 'warning' })
      .then(async () => {
        for (const id of selectedIds.value) {
          await IbmsDeviceApi.deleteDevice(id)
        }
        selectedIds.value = []
        ElMessage.success('删除成功')
        fetchPage()
      })
      .catch(() => {})
    return
  }
  ElMessage.info('该批量操作接口待接入')
}

const dialog = reactive({ visible: false, isEdit: false })
const formRef = ref<FormInstance>()
const form = reactive({
  id: 0 as number | undefined,
  group: '',
  system: '',
  deviceType: '',
  brand: '',
  accessType: '',
  name: '',
  seq: '001',
  ip: '',
  protocol: '',
  productModel: '',
  productKey: '',
  /** 当前设备已有 extra，用于与产品模板合并 */
  extraStr: ''
})

const templateProduct = ref<IbmsProductRespVO | null>(null)
const templateLoading = ref(false)
/** 动态表单值，键为产品属性 propName */
const extraValues = reactive<Record<string, string>>({})
const extraManualJson = ref('')
/** 高级 JSON 折叠面板，默认收起 */
const extraAdvancedCollapse = ref<string[]>([])
const templatePointTotal = computed(() =>
  (templateProduct.value?.pointTypes || []).reduce((sum, pt) => sum + (pt.count || 0), 0)
)

/** 接入类型「IP」对应的扩展字段（写入 extra，键名与网关 NVR 侧一致） */
const accessIp = reactive({
  tcpPort: '',
  httpPort: '',
  rtspPort: '',
  username: '',
  password: ''
})

/** IP + Modbus TCP：从站地址（Unit ID） */
const accessModbusTcp = reactive({
  unitId: ''
})

/** IP + BACnet/IP */
const accessBacnet = reactive({
  udpPort: '',
  deviceInstance: ''
})

/** IP + MQTT（Broker 端口与主题等，与 ONVIF 的 TCP/HTTP/RTSP 区分） */
const accessIpMqtt = reactive({
  port: '',
  clientId: '',
  subscribeTopic: '',
  publishTopic: '',
  useTls: false
})

const isIpProtocolVideo = computed(
  () => form.accessType === 'IP' && (form.protocol === 'ONVIF' || form.protocol === 'GB28181')
)
const isIpProtocolModbusTcp = computed(() => form.accessType === 'IP' && form.protocol === 'Modbus TCP')
const isIpProtocolBacnet = computed(() => form.accessType === 'IP' && form.protocol === 'BACnet')
const isIpProtocolMqtt = computed(() => form.accessType === 'IP' && form.protocol === 'MQTT')
/** 未选择接入协议时展示通用端口表单，兼容历史数据 */
const isIpProtocolGeneric = computed(() => form.accessType === 'IP' && !form.protocol)

const ipHostLabel = computed(() => (isIpProtocolMqtt.value ? 'Broker 地址' : 'IP 地址'))
const ipHostPlaceholder = computed(() =>
  isIpProtocolMqtt.value ? '如 mqtt.example.com 或 192.168.1.10' : '如 192.168.1.10'
)

/** 接入类型「RS485」/ Modbus RTU 等 */
const accessRs485 = reactive({
  serialPort: '',
  baudRate: '9600',
  dataBits: '8',
  stopBits: '1',
  parity: 'N',
  slaveId: ''
})

/** 韦根 */
const accessWiegand = reactive({
  format: '34',
  direction: 'BOTH',
  readerNo: '',
  doorIndex: '',
  invert: false
})

/** 无线 */
const accessWireless = reactive({
  protocol: '',
  deviceEui: '',
  gatewayId: '',
  panId: '',
  rfChannel: '',
  ssid: '',
  appKey: ''
})

/** 模拟量 */
const accessAnalog = reactive({
  signalType: '4-20mA',
  inputNo: '',
  unit: '',
  rangeMin: '',
  rangeMax: '',
  adcBits: '16'
})

/** 开关量 */
const accessDigital = reactive({
  inputNo: '',
  contactType: 'NO',
  activeLevel: 'HIGH',
  debounceMs: '20',
  pollingMs: '',
  moduleAddress: ''
})

const resetAccessParams = () => {
  accessIp.tcpPort = ''
  accessIp.httpPort = ''
  accessIp.rtspPort = ''
  accessIp.username = ''
  accessIp.password = ''
  accessModbusTcp.unitId = ''
  accessBacnet.udpPort = ''
  accessBacnet.deviceInstance = ''
  accessIpMqtt.port = ''
  accessIpMqtt.clientId = ''
  accessIpMqtt.subscribeTopic = ''
  accessIpMqtt.publishTopic = ''
  accessIpMqtt.useTls = false
  accessRs485.serialPort = ''
  accessRs485.baudRate = '9600'
  accessRs485.dataBits = '8'
  accessRs485.stopBits = '1'
  accessRs485.parity = 'N'
  accessRs485.slaveId = ''
  accessWiegand.format = '34'
  accessWiegand.direction = 'BOTH'
  accessWiegand.readerNo = ''
  accessWiegand.doorIndex = ''
  accessWiegand.invert = false
  accessWireless.protocol = ''
  accessWireless.deviceEui = ''
  accessWireless.gatewayId = ''
  accessWireless.panId = ''
  accessWireless.rfChannel = ''
  accessWireless.ssid = ''
  accessWireless.appKey = ''
  accessAnalog.signalType = '4-20mA'
  accessAnalog.inputNo = ''
  accessAnalog.unit = ''
  accessAnalog.rangeMin = ''
  accessAnalog.rangeMax = ''
  accessAnalog.adcBits = '16'
  accessDigital.inputNo = ''
  accessDigital.contactType = 'NO'
  accessDigital.activeLevel = 'HIGH'
  accessDigital.debounceMs = '20'
  accessDigital.pollingMs = ''
  accessDigital.moduleAddress = ''
}

/** 从已保存的 extra JSON 回填接入参数表单 */
const hydrateAccessFromExtraStr = (raw?: string) => {
  const p = safeParseExtraRecord(raw)
  accessIp.tcpPort = p.tcpPort ?? ''
  accessIp.httpPort = p.httpPort ?? ''
  accessIp.rtspPort = p.rtspPort ?? ''
  accessIp.username = p.username ?? ''
  accessIp.password = p.password ?? ''
  accessModbusTcp.unitId = p.modbusUnitId ?? p.unitId ?? p.slaveId ?? ''
  accessBacnet.udpPort = p.bacnetPort ?? p.bacnetUdpPort ?? ''
  accessBacnet.deviceInstance = p.bacnetDeviceInstance ?? ''
  accessIpMqtt.port =
    p.mqttPort ?? p.mqttBrokerPort ?? (form.protocol === 'MQTT' ? (p.tcpPort ?? '') : '')
  accessIpMqtt.clientId = p.mqttClientId ?? ''
  accessIpMqtt.subscribeTopic = p.mqttSubscribeTopic ?? ''
  accessIpMqtt.publishTopic = p.mqttPublishTopic ?? ''
  accessIpMqtt.useTls = p.mqttUseTls === 'true' || p.mqttTls === 'true'
  accessRs485.serialPort = p.serialPort ?? p.comPort ?? ''
  const br = p.baudRate ?? ''
  accessRs485.baudRate = ['9600', '19200', '38400', '57600', '115200'].includes(br) ? br : '9600'
  accessRs485.slaveId = p.slaveId ?? ''
  accessRs485.dataBits = p.dataBits === '7' ? '7' : '8'
  accessRs485.stopBits = p.stopBits === '2' ? '2' : '1'
  accessRs485.parity = ['N', 'E', 'O'].includes(p.parity ?? '') ? (p.parity as string) : 'N'

  accessWiegand.format = ['26', '34', '37'].includes(p.wiegandFormat ?? '') ? p.wiegandFormat! : '34'
  accessWiegand.direction = ['IN', 'OUT', 'BOTH'].includes(p.wiegandDirection ?? '')
    ? p.wiegandDirection!
    : 'BOTH'
  accessWiegand.readerNo = p.wiegandReaderNo ?? ''
  accessWiegand.doorIndex = p.wiegandDoorIndex ?? ''
  accessWiegand.invert = p.wiegandInvert === 'true'

  accessWireless.protocol = p.wirelessProtocol ?? ''
  accessWireless.deviceEui = p.wirelessDeviceEui ?? ''
  accessWireless.gatewayId = p.wirelessGatewayId ?? ''
  accessWireless.panId = p.wirelessPanId ?? ''
  accessWireless.rfChannel = p.wirelessRfChannel ?? ''
  accessWireless.ssid = p.wirelessSsid ?? ''
  accessWireless.appKey = p.wirelessAppKey ?? ''

  const st = p.analogSignalType ?? ''
  accessAnalog.signalType = ['4-20mA', '0-10V', '0-5V', 'PT100', 'NTC', 'Other'].includes(st) ? st : '4-20mA'
  accessAnalog.inputNo = p.analogInputNo ?? ''
  accessAnalog.unit = p.analogUnit ?? ''
  accessAnalog.rangeMin = p.analogRangeMin ?? ''
  accessAnalog.rangeMax = p.analogRangeMax ?? ''
  const adc = p.analogAdcBits ?? ''
  accessAnalog.adcBits = ['12', '16', '24', 'auto'].includes(adc) ? adc : '16'

  accessDigital.inputNo = p.dioInputNo ?? ''
  accessDigital.contactType = ['NO', 'NC'].includes(p.dioContactType ?? '') ? p.dioContactType! : 'NO'
  accessDigital.activeLevel = ['HIGH', 'LOW'].includes(p.dioActiveLevel ?? '') ? p.dioActiveLevel! : 'HIGH'
  accessDigital.debounceMs = p.dioDebounceMs ?? '20'
  accessDigital.pollingMs = p.dioPollingMs ?? ''
  accessDigital.moduleAddress = p.dioModuleAddress ?? ''
}

const buildAccessExtraObject = (): Record<string, string> => {
  const o: Record<string, string> = {}
  if (form.accessType === 'IP') {
    const proto = form.protocol
    if (proto === 'ONVIF' || proto === 'GB28181') {
      if (accessIp.tcpPort.trim()) o.tcpPort = accessIp.tcpPort.trim()
      if (accessIp.httpPort.trim()) o.httpPort = accessIp.httpPort.trim()
      if (accessIp.rtspPort.trim()) o.rtspPort = accessIp.rtspPort.trim()
      if (accessIp.username.trim()) o.username = accessIp.username.trim()
      if (accessIp.password.trim()) o.password = accessIp.password.trim()
    } else if (proto === 'Modbus TCP') {
      if (accessIp.tcpPort.trim()) o.tcpPort = accessIp.tcpPort.trim()
      if (accessModbusTcp.unitId.trim()) o.modbusUnitId = accessModbusTcp.unitId.trim()
    } else if (proto === 'BACnet') {
      if (accessBacnet.udpPort.trim()) o.bacnetPort = accessBacnet.udpPort.trim()
      if (accessBacnet.deviceInstance.trim()) o.bacnetDeviceInstance = accessBacnet.deviceInstance.trim()
    } else if (proto === 'MQTT') {
      if (accessIpMqtt.port.trim()) o.mqttPort = accessIpMqtt.port.trim()
      if (accessIpMqtt.clientId.trim()) o.mqttClientId = accessIpMqtt.clientId.trim()
      if (accessIpMqtt.subscribeTopic.trim()) o.mqttSubscribeTopic = accessIpMqtt.subscribeTopic.trim()
      if (accessIpMqtt.publishTopic.trim()) o.mqttPublishTopic = accessIpMqtt.publishTopic.trim()
      if (accessIpMqtt.useTls) o.mqttUseTls = 'true'
      if (accessIp.username.trim()) o.username = accessIp.username.trim()
      if (accessIp.password.trim()) o.password = accessIp.password.trim()
    } else {
      if (accessIp.tcpPort.trim()) o.tcpPort = accessIp.tcpPort.trim()
      if (accessIp.httpPort.trim()) o.httpPort = accessIp.httpPort.trim()
      if (accessIp.rtspPort.trim()) o.rtspPort = accessIp.rtspPort.trim()
      if (accessIp.username.trim()) o.username = accessIp.username.trim()
      if (accessIp.password.trim()) o.password = accessIp.password.trim()
    }
  } else if (form.accessType === 'RS485') {
    if (accessRs485.serialPort.trim()) o.serialPort = accessRs485.serialPort.trim()
    o.baudRate = accessRs485.baudRate
    if (accessRs485.slaveId.trim()) o.slaveId = accessRs485.slaveId.trim()
    o.dataBits = accessRs485.dataBits
    o.stopBits = accessRs485.stopBits
    o.parity = accessRs485.parity
  } else if (form.accessType === '韦根') {
    o.wiegandFormat = accessWiegand.format
    o.wiegandDirection = accessWiegand.direction
    if (accessWiegand.readerNo.trim()) o.wiegandReaderNo = accessWiegand.readerNo.trim()
    if (accessWiegand.doorIndex.trim()) o.wiegandDoorIndex = accessWiegand.doorIndex.trim()
    o.wiegandInvert = accessWiegand.invert ? 'true' : 'false'
  } else if (form.accessType === '无线') {
    if (accessWireless.protocol.trim()) o.wirelessProtocol = accessWireless.protocol.trim()
    if (accessWireless.deviceEui.trim()) o.wirelessDeviceEui = accessWireless.deviceEui.trim()
    if (accessWireless.gatewayId.trim()) o.wirelessGatewayId = accessWireless.gatewayId.trim()
    if (accessWireless.panId.trim()) o.wirelessPanId = accessWireless.panId.trim()
    if (accessWireless.rfChannel.trim()) o.wirelessRfChannel = accessWireless.rfChannel.trim()
    if (accessWireless.ssid.trim()) o.wirelessSsid = accessWireless.ssid.trim()
    if (accessWireless.appKey.trim()) o.wirelessAppKey = accessWireless.appKey.trim()
  } else if (form.accessType === '模拟量') {
    o.analogSignalType = accessAnalog.signalType
    if (accessAnalog.inputNo.trim()) o.analogInputNo = accessAnalog.inputNo.trim()
    if (accessAnalog.unit.trim()) o.analogUnit = accessAnalog.unit.trim()
    if (accessAnalog.rangeMin.trim()) o.analogRangeMin = accessAnalog.rangeMin.trim()
    if (accessAnalog.rangeMax.trim()) o.analogRangeMax = accessAnalog.rangeMax.trim()
    if (accessAnalog.adcBits && accessAnalog.adcBits !== 'auto') o.analogAdcBits = accessAnalog.adcBits
  } else if (form.accessType === '开关量') {
    if (accessDigital.inputNo.trim()) o.dioInputNo = accessDigital.inputNo.trim()
    o.dioContactType = accessDigital.contactType
    o.dioActiveLevel = accessDigital.activeLevel
    if (accessDigital.debounceMs.trim()) o.dioDebounceMs = accessDigital.debounceMs.trim()
    if (accessDigital.pollingMs.trim()) o.dioPollingMs = accessDigital.pollingMs.trim()
    if (accessDigital.moduleAddress.trim()) o.dioModuleAddress = accessDigital.moduleAddress.trim()
  }
  return o
}

// ===== 通道列表相关状态 =====
const channelList = ref<IbmsChannelRespVO[]>([])
const channelLoading = ref(false)
const channelSyncing = ref(false)

const loadChannels = async (deviceId: number) => {
  channelLoading.value = true
  try {
    channelList.value = (await IbmsChannelApi.listChannelsByDevice(deviceId)) || []
  } catch {
    channelList.value = []
  } finally {
    channelLoading.value = false
  }
}

const syncChannels = async () => {
  if (!form.id) return
  channelSyncing.value = true
  try {
    channelList.value = (await IbmsChannelApi.syncChannelsFromDevice(form.id)) || []
    ElMessage.success(`同步完成，共 ${channelList.value.length} 个通道`)
  } catch (e: any) {
    ElMessage.error(e?.message || '获取通道失败，请确认设备已在 IoT 平台连接')
  } finally {
    channelSyncing.value = false
  }
}

const clearChannelState = () => {
  channelList.value = []
}

const clearExtraState = () => {
  templateProduct.value = null
  Object.keys(extraValues).forEach((k) => delete extraValues[k])
  extraManualJson.value = ''
}

const safeParseExtraRecord = (raw?: string): Record<string, string> => {
  if (!raw || !String(raw).trim()) return {}
  try {
    const o = JSON.parse(raw) as Record<string, unknown>
    if (!o || typeof o !== 'object' || Array.isArray(o)) return {}
    const out: Record<string, string> = {}
    for (const [k, v] of Object.entries(o)) {
      if (v === null || v === undefined) continue
      out[k] = typeof v === 'object' ? JSON.stringify(v) : String(v)
    }
    return out
  } catch {
    return {}
  }
}

const syncManualJsonFromForm = () => {
  if (!form.extraStr?.trim()) {
    extraManualJson.value = ''
    return
  }
  try {
    extraManualJson.value = JSON.stringify(JSON.parse(form.extraStr), null, 2)
  } catch {
    extraManualJson.value = form.extraStr
  }
}

const propertySelectOptions = (prop: IbmsProductPropertyVO): string[] => {
  const opt = prop.options
  if (!opt || !String(opt).trim()) return []
  try {
    const v = JSON.parse(opt) as unknown
    return Array.isArray(v) ? v.map((x) => String(x)) : []
  } catch {
    return []
  }
}

const propertyLabel = (prop: IbmsProductPropertyVO) => {
  const u = prop.unit ? `（${prop.unit}）` : ''
  return `${prop.label}${u}`
}

const applyTemplateToExtraValues = (product: IbmsProductRespVO) => {
  const parsed = safeParseExtraRecord(form.extraStr)
  const next: Record<string, string> = {}
  for (const p of product.properties || []) {
    const key = p.propName
    const raw = parsed[key]
    next[key] =
      raw !== undefined && raw !== null && String(raw) !== '' ? String(raw) : (p.defaultValue ?? '')
  }
  Object.keys(extraValues).forEach((k) => delete extraValues[k])
  Object.assign(extraValues, next)
}

const loadProductTemplate = useDebounceFn(async () => {
  const { group, system, deviceType, productModel } = form
  if (!group || !system || !deviceType || !String(productModel || '').trim()) {
    clearExtraState()
    syncManualJsonFromForm()
    return
  }
  templateLoading.value = true
  try {
    const data = await IbmsProductApi.resolveTemplateForDevice({
      groupCode: group,
      systemCode: system,
      deviceTypeCode: deviceType,
      modelNumber: String(productModel).trim()
    })
    templateProduct.value = data
    if (data?.properties?.length) {
      applyTemplateToExtraValues(data)
      extraManualJson.value = ''
    } else {
      Object.keys(extraValues).forEach((k) => delete extraValues[k])
      syncManualJsonFromForm()
    }
  } catch {
    templateProduct.value = null
    Object.keys(extraValues).forEach((k) => delete extraValues[k])
    syncManualJsonFromForm()
  } finally {
    templateLoading.value = false
  }
}, 280)

watch(
  () => [form.group, form.system, form.deviceType, form.productModel],
  () => {
    if (!dialog.visible) return
    loadProductTemplate()
  }
)

const buildExtraPayload = (): string | undefined => {
  const accessPart = buildAccessExtraObject()

  let fromManual: Record<string, unknown> = {}
  const manualRaw = extraManualJson.value.trim()
  if (manualRaw) {
    try {
      const parsed = JSON.parse(manualRaw) as unknown
      if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
        throw new Error('invalid')
      }
      fromManual = parsed as Record<string, unknown>
    } catch {
      throw new Error('JSON')
    }
  }

  const productPart: Record<string, string> = templateProduct.value?.properties?.length
    ? { ...extraValues }
    : {}

  // 合并：手动 JSON 打底，接入参数与产品字段覆盖同名键（表单为准）
  const merged: Record<string, unknown> = { ...fromManual, ...accessPart, ...productPart }

  const cleaned: Record<string, unknown> = {}
  for (const [k, v] of Object.entries(merged)) {
    if (v === undefined || v === null) continue
    if (typeof v === 'string' && !v.trim()) continue
    cleaned[k] = typeof v === 'string' ? v.trim() : v
  }

  if (Object.keys(cleaned).length === 0) return undefined
  return JSON.stringify(cleaned)
}

const productModelLoading = ref(false)
const productModelOptions = ref<string[]>([])

const loadProductModelOptions = async (keyword = '') => {
  productModelLoading.value = true
  try {
    const res: any = await IbmsProductApi.getProductPage({
      pageNo: 1,
      pageSize: 100,
      productName: keyword || undefined,
      groupCode: form.group || undefined,
      systemCode: form.system || undefined,
      deviceTypeCode: form.deviceType || undefined
    })
    const list = res?.list || res?.data?.list || []
    const map = new Set<string>()
    list.forEach((p: any) => {
      const v = String(p?.modelNumber || '').trim()
      if (v) map.add(v)
    })
    productModelOptions.value = Array.from(map)
  } finally {
    productModelLoading.value = false
  }
}

const onProductModelVisibleChange = (visible: boolean) => {
  if (!visible) return
  if (productModelOptions.value.length) return
  loadProductModelOptions()
}

const filteredSystemOptionsForForm = computed(() => {
  if (!form.group) return systemOptions.value
  return systemOptions.value.filter((s) => {
    const rm = parseDictRemark<{ group?: string }>(s.remark)
    return !rm?.group || rm.group === form.group
  })
})

const onFormGroupChange = () => {
  form.system = ''
  form.productModel = ''
  productModelOptions.value = []
}

const onFormSystemChange = () => {
  form.productModel = ''
  productModelOptions.value = []
}

const rules: FormRules = {
  group: [{ required: true, message: '请选择专业分组', trigger: 'change' }],
  system: [{ required: true, message: '请选择系统', trigger: 'change' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  brand: [{ required: true, message: '请选择品牌', trigger: 'change' }],
  accessType: [{ required: true, message: '请选择接入类型', trigger: 'change' }],
  productModel: [{ required: true, message: '请输入或选择产品型号', trigger: 'change' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  seq: [{ required: true, message: '请输入 3 位序号', trigger: 'blur' }]
}

const openCreate = () => {
  dialog.isEdit = false
  dialog.visible = true
  clearExtraState()
  resetAccessParams()
  clearChannelState()
  Object.assign(form, {
    id: undefined,
    group: '',
    system: '',
    deviceType: '',
    brand: '',
    accessType: '',
    name: '',
    seq: '001',
    ip: '',
    protocol: '',
    productModel: '',
    productKey: '',
    extraStr: ''
  })
  formRef.value?.clearValidate()
  loadProductTemplate()
}

const openEdit = (row: DeviceRow) => {
  dialog.isEdit = true
  dialog.visible = true
  clearChannelState()
  Object.assign(form, {
    id: row.id,
    group: row.group,
    system: row.system,
    deviceType: row.deviceType,
    brand: row.brand,
    accessType: row.accessType,
    name: row.name,
    seq: row.code.split('-').pop() || '001',
    ip: row.ip,
    protocol: row.protocol,
    productModel: row.productModel,
    productKey: row.productKey || '',
    extraStr: row.extra || ''
  })
  hydrateAccessFromExtraStr(form.extraStr)
  formRef.value?.clearValidate()
  loadProductTemplate()
  loadChannels(row.id)
}

const submitForm = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    let extraPayload: string | undefined
    try {
      extraPayload = buildExtraPayload()
    } catch {
      ElMessage.error('扩展 JSON 不是合法的对象格式，请检查')
      return
    }
    const seqNum = parseInt(String(form.seq || '1'), 10) || 1
    const isIpAccess = form.accessType === 'IP'
    const payload: IbmsDeviceApi.IbmsDeviceSaveReqVO = {
      id: form.id,
      name: form.name,
      groupCode: form.group,
      systemCode: form.system,
      deviceTypeCode: form.deviceType,
      brand: form.brand,
      accessType: form.accessType,
      productModel: form.productModel,
      // 仅 IP 接入写入设备表 ip/protocol；RS485(Modbus RTU)/韦根/无线等不应带设备级 IP
      ip: isIpAccess ? (form.ip?.trim() || undefined) : undefined,
      protocol: isIpAccess ? (form.protocol || undefined) : undefined,
      space: 'F01 大堂',
      seq: seqNum,
      extra: extraPayload
    }
    if (dialog.isEdit) {
      await IbmsDeviceApi.updateDevice(payload)
      ElMessage.success('更新成功')
      dialog.visible = false
      fetchPage()
    } else {
      const newId = await IbmsDeviceApi.createDevice(payload)
      ElMessage.success('创建成功，可点击「获取通道」同步通道数据')
      // 保存后切换为编辑态，弹窗不关闭，让用户可以直接获取通道
      form.id = newId
      dialog.isEdit = true
      loadChannels(newId)
      fetchPage()
    }
  })
}

const deleteRow = (row: DeviceRow) => {
  ElMessageBox.confirm(`确认删除设备「${row.name}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await IbmsDeviceApi.deleteDevice(row.id)
      selectedIds.value = selectedIds.value.filter((id) => id !== row.id)
      ElMessage.success('删除成功')
      fetchPage()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchPage()
})
</script>

<style scoped lang="scss">
.ibms-device-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ibms-device-page__header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .right {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.glass-panel {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #34d399;
  }

  .text {
    color: rgba(226, 232, 240, 0.9);
    font-size: 13px;
  }

  .sub {
    color: rgba(148, 163, 184, 0.9);
    font-size: 12px;
  }
}

.icon-btn {
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: rgba(226, 232, 240, 0.9);
}

.tabs {
  display: flex;
  gap: 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
  padding-bottom: 6px;
  overflow: auto;

  .tab-btn {
    padding: 6px 10px;
    border-bottom: 2px solid transparent;
    border-radius: 0;
    color: rgba(148, 163, 184, 0.95);

    &.active {
      color: #3b82f6;
      border-bottom-color: #3b82f6;
    }
  }
}

.stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;

  .icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;

    &.blue {
      background: rgba(59, 130, 246, 0.22);
      color: rgba(147, 197, 253, 0.95);
    }
    &.green {
      background: rgba(16, 185, 129, 0.22);
      color: rgba(110, 231, 183, 0.95);
    }
    &.indigo {
      background: rgba(99, 102, 241, 0.22);
      color: rgba(165, 180, 252, 0.95);
    }
    &.emerald {
      background: rgba(34, 197, 94, 0.22);
      color: rgba(134, 239, 172, 0.95);
    }
    &.amber {
      background: rgba(245, 158, 11, 0.22);
      color: rgba(252, 211, 77, 0.95);
    }
  }

  .num {
    font-size: 22px;
    font-weight: 800;
    color: rgba(255, 255, 255, 0.95);
    line-height: 1.1;
  }

  .label {
    margin-top: 2px;
    font-size: 12px;
    color: rgba(148, 163, 184, 0.95);
  }

  .green-text {
    color: rgba(110, 231, 183, 0.95);
  }
  .indigo-text {
    color: rgba(165, 180, 252, 0.95);
  }
  .emerald-text {
    color: rgba(134, 239, 172, 0.95);
  }
  .amber-text {
    color: rgba(252, 211, 77, 0.95);
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  .left,
  .right {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.glass-btn {
  background: rgba(51, 65, 85, 0.35);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: rgba(226, 232, 240, 0.9);
}

.btn-purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  border-color: rgba(139, 92, 246, 0.5);
}

.rule-tip {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  font-size: 12px;
  color: rgba(148, 163, 184, 0.95);
  display: flex;
  align-items: center;
}

.advanced {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);

  .field {
    .label {
      font-size: 12px;
      color: rgba(148, 163, 184, 0.95);
      margin-bottom: 6px;
    }
  }
}

.cell-main {
  .name {
    font-weight: 700;
    color: rgba(226, 232, 240, 0.95);
  }

  .code {
    margin-top: 4px;
    font-size: 12px;
    color: rgba(148, 163, 184, 0.95);
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
      monospace;
  }
}

.table-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  .total {
    font-size: 13px;
    color: rgba(148, 163, 184, 0.95);
  }
}

.batch-bar {
  position: sticky;
  bottom: 0;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  z-index: 10;

  .left,
  .right {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .count {
    color: rgba(96, 165, 250, 0.95);
    font-weight: 800;
  }

  .clear {
    color: rgba(148, 163, 184, 0.95);
  }
}

.danger-btn {
  background: rgba(239, 68, 68, 0.18);
  border: 1px solid rgba(239, 68, 68, 0.35);
  color: rgba(252, 165, 165, 0.95);
}

.text-blue {
  color: rgba(96, 165, 250, 0.95);
}

.extra-tip {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.95);
  margin-bottom: 10px;
}

.extra-head {
  font-size: 13px;
  color: rgba(226, 232, 240, 0.92);
  margin-bottom: 12px;

  .muted {
    margin-left: 6px;
    color: rgba(148, 163, 184, 0.9);
    font-weight: 400;
  }
}

.point-preview {
  margin-bottom: 10px;
}

.point-preview__head {
  margin-bottom: 8px;
  font-size: 13px;
  color: rgba(226, 232, 240, 0.92);
}

.point-preview__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.extra-fallback {
  margin-bottom: 4px;
}

.channel-block {
  margin: 12px 0 16px;
  padding: 12px 12px 10px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(15, 23, 42, 0.35);
}

.channel-block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.channel-block__title-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}

.channel-block__title {
  font-size: 13px;
  font-weight: 600;
  color: rgba(226, 232, 240, 0.95);
}

.channel-count {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
}

.channel-hint {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.75);
}

.channel-tip-inline {
  margin-bottom: 6px;
}

.channel-table {
  width: 100%;
}

.channel-empty {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.75);
  padding: 8px 0 4px;
}

.access-params-card {
  margin-bottom: 14px;
  background: rgba(15, 23, 42, 0.25);
  border: 1px solid rgba(148, 163, 184, 0.18);

  :deep(.el-card__header) {
    padding: 10px 14px;
    font-size: 13px;
    font-weight: 600;
    color: rgba(226, 232, 240, 0.92);
  }

  :deep(.el-card__body) {
    padding: 12px 14px 6px;
  }
}

.access-params-card__sub {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: rgba(148, 163, 184, 0.85);
}

.access-params-tip {
  margin-top: 0;
  margin-bottom: 10px;
}

.extra-advanced-collapse {
  margin-bottom: 8px;
  border: none;
  background: transparent;

  :deep(.el-collapse-item__header) {
    font-size: 12px;
    color: rgba(148, 163, 184, 0.95);
    background: transparent;
    border: none;
  }

  :deep(.el-collapse-item__wrap) {
    background: transparent;
    border: none;
  }
}
</style>

