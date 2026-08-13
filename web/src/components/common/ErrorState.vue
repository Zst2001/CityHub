<script setup>defineProps({ message: { type: String, default: '暂时无法加载内容，请稍后再试。' } }); defineEmits(['retry'])</script>
<template><div class="error-state"><p>{{ message }}</p><el-button plain @click="$emit('retry')">重新尝试</el-button></div></template>
<style scoped>.error-state { padding:var(--space-6); border:1px dashed var(--color-border); border-radius:var(--radius-lg); text-align:center; color:var(--color-text-secondary); }.error-state p { margin:0 0 var(--space-4); }</style>
