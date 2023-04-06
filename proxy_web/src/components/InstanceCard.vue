<template>
  <q-card style="cursor: pointer" @click="clickCard">
    <q-card-section class="text-center bg-primary text-white">
      实例ID
      <br>
      <strong>{{ rowData.row.instance + '(' + rowData.row.count + ')' }}</strong>
    </q-card-section>
    <q-separator />
    <q-card-section class="flex flex-center">
      <div>{{ rowData.row.address }} </div>
    </q-card-section>
  </q-card>
</template>

<script>
    export default {
      props: {
        rowData: {
          type: Object
        }
      },
      name: "InstanceCard",
      methods:{
        clickCard(e){
          this.$emit('clickCard',this.rowData)
        }
      }
    }
</script>

<style scoped>

</style>
