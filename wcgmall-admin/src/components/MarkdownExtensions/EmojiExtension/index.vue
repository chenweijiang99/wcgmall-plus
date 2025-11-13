<template>
    <DropdownToolbar title="emoji" :visible="state.visible" @onChange="onChange">
        <template #overlay>
            <div class="emoji-container">
                <ol class="emojis">
                    <li v-for="(emoji, index) of emojis" :key="`emoji-${index}`" @click="emojiHandler(emoji.symbol)"
                        v-text="emoji.symbol" :title="emoji.name" ></li>
                </ol>
            </div>
        </template>
        <template #trigger>

            <svg class="bi bi-emoji-smile" width="1em" height="1em" viewBox="0 0 16 16" fill="currentColor"
                xmlns="http://www.w3.org/2000/svg" stroke="currentColor" stroke-width="0.5">
                <path fill-rule="evenodd" d="M8 15A7 7 0 1 0 8 1a7 7 0 0 0 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z" />
                <path fill-rule="evenodd"
                    d="M4.285 9.567a.5.5 0 0 1 .683.183A3.498 3.498 0 0 0 8 11.5a3.498 3.498 0 0 0 3.032-1.75.5.5 0 1 1 .866.5A4.498 4.498 0 0 1 8 12.5a4.498 4.498 0 0 1-3.898-2.25.5.5 0 0 1 .183-.683z" />
                <path
                    d="M7 6.5C7 7.328 6.552 8 6 8s-1-.672-1-1.5S5.448 5 6 5s1 .672 1 1.5zm4 0c0 .828-.448 1.5-1 1.5s-1-.672-1-1.5S9.448 5 10 5s1 .672 1 1.5z" />
            </svg>
        </template>
    </DropdownToolbar>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import type { PropType } from 'vue';
import type { InsertContentGenerator  } from 'md-editor-v3';
import { emojis } from './data';
import { DropdownToolbar } from 'md-editor-v3';
const props = defineProps({
    onInsert: {
        type: Function as PropType<(generator: InsertContentGenerator) => void>,
        default: () => () => null
    }
});

const state = reactive({
    visible: false
});

const emojiHandler = (emoji: string) => {
    const generator: InsertContentGenerator = () => {
        return {
            targetValue: emoji,
            select: false,
            deviationStart: 0,
            deviationEnd: 0
        };
    };

    props.onInsert(generator);
};

const onChange = (visible: boolean) => {
    state.visible = visible;
};
</script>

<script lang="ts">
export default {
    name: 'EmojiExtension'
};
</script>

<style lang="scss" scoped>
.emoji-container {
  border-radius: 3px;
  border: 1px solid #e6e6e6;
  max-height: 300px;
  overflow-y: auto;
}

.emojis {
  position: relative;
  width: 363px;
  margin: 10px;
  padding: 0;
  background-color: #fff;

  li {
    cursor: pointer;
    float: left;
    border: 1px solid #e8e8e8;
    height: 24px;
    width: 28px;
    overflow: hidden;
    margin: -1px 0 0 -1px;
    padding: 4px 2px;
    text-align: center;
    list-style: none;
    z-index: 11;

    &:hover {
      position: relative;
      border: 1px solid #63a35c;
      z-index: 12;
    }
  }

  &::after {
    content: '';
    clear: left;
    display: block;
  }

  * {
    user-select: none;
  }
}
</style>