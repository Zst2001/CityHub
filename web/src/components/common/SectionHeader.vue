<script setup>defineProps({ eyebrow: String, title: { type: String, required: true }, description: String })</script>
<template><header class="section-header"><span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span><h2>{{ title }}</h2><p v-if="description">{{ description }}</p><slot name="action" /></header></template>
<style scoped>.section-header { margin-bottom:var(--space-5); }.section-header h2 { margin:var(--space-2) 0; font-size:clamp(1.45rem, 2vw, 2rem); letter-spacing:-.05em; }.section-header p { margin:0; color:var(--color-text-secondary); }</style>
