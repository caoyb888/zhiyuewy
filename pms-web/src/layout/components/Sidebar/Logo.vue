<template>
  <div class="sidebar-logo-container" :class="{'collapse':collapse}" :style="{ backgroundColor: logoBgColor, borderBottomColor: logoBorderColor }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />     
        <h1 v-else class="sidebar-title" :style="{ color: logoTextColor }">{{ title }} </h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 class="sidebar-title" :style="{ color: logoTextColor }">{{ title }} </h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
import logoImg from '@/assets/logo/logo.png'
import variables from '@/assets/styles/variables.scss'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    variables() {
      return variables;
    },
    sideTheme() {
      return this.$store.state.settings.sideTheme
    },
    logoBgColor() {
      const map = {
        'theme-dark': '#031e39',
        'theme-light': '#ffffff',
        'theme-vitality': '#1e293b'
      }
      return map[this.sideTheme] || '#ffffff'
    },
    logoTextColor() {
      const map = {
        'theme-dark': '#ffffff',
        'theme-light': '#FF6B35',
        'theme-vitality': '#FF6B35'
      }
      return map[this.sideTheme] || '#FF6B35'
    },
    logoBorderColor() {
      const map = {
        'theme-dark': '#031e39',
        'theme-light': '#f1f5f9',
        'theme-vitality': '#334155'
      }
      return map[this.sideTheme] || '#f1f5f9'
    }
  },
  data() {
    return {
      title: '智悦物业系统',
      logo: logoImg,
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 60px;
  line-height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #f1f5f9;
  text-align: center;
  overflow: hidden;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;

    & .sidebar-logo {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 12px;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: #FF6B35;
      font-weight: 600;
      line-height: 50px;
      font-size: 18px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>
