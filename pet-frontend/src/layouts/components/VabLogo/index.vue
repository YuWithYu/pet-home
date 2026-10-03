<template>
  <div :class="'logo-container-' + layout">
    <router-link to="/">
      <!-- 这里是logo变更的位置：图片路径显示 img，否则显示默认 SVG 图标 -->
      <img
        v-if="logo && isLogoImage"
        :src="logoSrc"
        alt="logo"
        class="logo logo-img"
      />
      <svg
        v-else-if="logo"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        class="logo"
      >
        <path fill="none" d="M0 0h24v24H0z" />
        <path
          d="M1 3h4l7 12 7-12h4L12 22 1 3zm8.667 0L12 7l2.333-4h4.035L12 14 5.632 3h4.035z"
        />
      </svg>
      <span
        :class="{ 'hidden-xs-only': layout === 'horizontal' }"
        :title="title"
        class="title"
      >
        {{ title }}
      </span>
    </router-link>
  </div>
</template>
<script>
import { mapGetters } from "vuex";
// 使用打包内资源，避免 /static/ 在部分环境下无法访问导致裂图
import defaultLogo from "@/assets/pet-home-logo.png";

export default {
  name: "VabLogo",
  data() {
    return {
      title: this.$baseTitle,
      defaultLogo,
    };
  },
  computed: {
    ...mapGetters({
      logo: "settings/logo",
      layout: "settings/layout",
    }),
    isLogoImage() {
      if (typeof this.logo !== "string") return false;
      const s = this.logo;
      return (
        s.startsWith("/") ||
        s.startsWith("http") ||
        /\.(png|jpg|jpeg|gif|svg|webp)(\?|$)/i.test(s)
      );
    },
    // 优先使用打包后的 logo 资源，避免 public/static 未正确访问导致不显示
    logoSrc() {
      if (!this.logo || !this.isLogoImage) return "";
      const s = this.logo;
      if (s === "/static/pet-home-logo.png" || (s && s.includes("pet-home-logo.png"))) {
        return this.defaultLogo;
      }
      return s;
    },
  },
};
</script>
<style lang="scss" scoped>
@mixin container {
  position: relative;
  height: $base-top-bar-height;
  overflow: hidden;
  line-height: $base-top-bar-height;
  background: $base-menu-background;
}

@mixin logo {
  display: inline-block;
  width: 34px;
  height: 34px;
  margin-right: 3px;
  color: #fff !important;
  fill: #fff !important;
  vertical-align: middle;
}

.logo-img {
  object-fit: contain;
}

@mixin title {
  display: inline-block;
  overflow: hidden;
  font-size: 20px;
  line-height: 55px;
  color: $base-title-color;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.logo-container-horizontal {
  @include container;

  .logo {
    @include logo;
  }

  .title {
    @include title;
  }
}

.logo-container-vertical {
  @include container;

  height: $base-logo-height;
  line-height: $base-logo-height;
  text-align: center;

  .logo {
    @include logo;
    fill: #fff !important;
  }

  .title {
    @include title;

    max-width: calc(#{$base-left-menu-width} - 60px);
  }
}
</style>
