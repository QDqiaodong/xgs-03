<template>
    <div>
        <nav class="nav-bar">
            <div class="nav-container">
                <router-link to="/" class="nav-brand">
                    🌿 绿植养护交流园地
                </router-link>
                <ul class="nav-links">
                    <li><router-link to="/">首页</router-link></li>
                    <li><router-link to="/categories">植物百科</router-link></li>
                    <li><router-link to="/archives">我的绿植</router-link></li>
                    <li><router-link to="/posts">交流社区</router-link></li>
                    <li><router-link to="/favorites">我的收藏</router-link></li>
                </ul>
            </div>
        </nav>
        <main class="container">
            <router-view />
        </main>
    </div>
</template>

<script setup>
</script>
