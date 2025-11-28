<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { CreditCard, Truck, CheckCircle, Plus } from 'lucide-vue-next';

const router = useRouter();
const step = ref<'shipping' | 'payment' | 'success'>('shipping');
const selectedAddressId = ref('new');
const saveToAddressBook = ref(false);

const formData = ref({
  name: store.user?.name || '',
  email: store.user?.email || '',
  addressName: 'My Address',
  street: '',
  city: 'San Francisco',
  zip: '94103',
  phone: store.user?.phone || ''
});

const total = computed(() => store.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0));

onMounted(() => {
  if (store.cart.length === 0 && step.value !== 'success') {
    router.push('/cart');
  }
  // Initialize default address
  if (store.user && store.user.addresses.length > 0) {
    const defaultAddr = store.user.addresses.find(a => a.isDefault);
    selectedAddressId.value = defaultAddr ? defaultAddr.id : store.user.addresses[0].id;
  }
});

const handleSubmitShipping = () => {
  if (selectedAddressId.value === 'new' && saveToAddressBook.value && store.user) {
    addAddress({
      name: formData.value.addressName,
      street: formData.value.street,
      city: formData.value.city,
      zip: formData.value.zip,
      phone: formData.value.phone,
      isDefault: store.user.addresses.length === 0
    });
  }
  step.value = 'payment';
};

const handlePlaceOrder = () => {
  let finalShippingString = '';
  
  if (selectedAddressId.value === 'new') {
    finalShippingString = `${formData.value.street}, ${formData.value.city}, ${formData.value.zip}`;
  } else if (store.user) {
    const addr = store.user.addresses.find(a => a.id === selectedAddressId.value);
    if (addr) {
      finalShippingString = `${addr.street}, ${addr.city}, ${addr.zip}`;
    }
  }

  placeOrder(finalShippingString);
  step.value = 'success';
};
</script>

<template>
  <div v-if="step === 'success'" class="pt-32 pb-20 min-h-[80vh] flex flex-col items-center justify-center text-center px-4">
    <div class="w-20 h-20 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-6">
      <CheckCircle :size="40" />
    </div>
    <h1 class="text-4xl font-bold mb-4">Order Confirmed!</h1>
    <p class="text-gray-500 mb-8 max-w-md">
      Thank you for your purchase. We've sent a confirmation email to {{ formData.email }}.
    </p>
    <el-button type="primary" size="large" round class="!px-8 !text-base !font-bold" @click="router.push('/profile')">
      View Order
    </el-button>
  </div>

  <div v-else class="pt-24 pb-20 max-w-6xl mx-auto px-4 sm:px-6">
    <h1 class="text-3xl font-bold mb-12">Checkout</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
      <!-- Form Section -->
      <div class="lg:col-span-2">
        <div v-if="step === 'shipping'">
          <el-form :model="formData" label-position="top" size="large" @submit.prevent="handleSubmitShipping">
            <div class="bg-white dark:bg-zinc-900 p-8 rounded-3xl border border-gray-100 dark:border-zinc-800">
              <div class="flex items-center gap-3 mb-6">
                <Truck class="text-blue-600" />
                <h2 class="text-xl font-bold">Shipping Information</h2>
              </div>
              
              <!-- Contact Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8 pb-8 border-b border-gray-100 dark:border-zinc-800">
                <div class="md:col-span-2">
                  <el-form-item label="Full Name" required>
                    <el-input v-model="formData.name" />
                  </el-form-item>
                </div>
                <div class="md:col-span-2">
                   <el-form-item label="Email" required>
                    <el-input v-model="formData.email" type="email" />
                  </el-form-item>
                </div>
              </div>

              <!-- Address Selection -->
              <div v-if="store.user && store.user.addresses.length > 0" class="mb-8">
                <label class="block text-sm font-medium mb-4">Choose Address</label>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div 
                    v-for="addr in store.user.addresses" 
                    :key="addr.id"
                    @click="selectedAddressId = addr.id"
                    :class="`p-4 rounded-xl border-2 cursor-pointer transition-all relative ${selectedAddressId === addr.id ? 'border-blue-600 bg-blue-50 dark:bg-blue-900/20' : 'border-gray-200 dark:border-zinc-800 hover:border-gray-300'}`"
                  >
                    <div class="font-bold mb-1">{{ addr.name }}</div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">{{ addr.street }}, {{ addr.city }}</div>
                    <span v-if="addr.isDefault" class="absolute top-2 right-2 w-2 h-2 bg-blue-600 rounded-full"></span>
                  </div>
                  <div 
                    @click="selectedAddressId = 'new'"
                    :class="`p-4 rounded-xl border-2 cursor-pointer transition-all flex flex-col items-center justify-center gap-2 text-gray-500 ${selectedAddressId === 'new' ? 'border-blue-600 bg-blue-50 dark:bg-blue-900/20 text-blue-600' : 'border-dashed border-gray-300 dark:border-zinc-700 hover:border-gray-400'}`"
                  >
                    <Plus :size="20" />
                    <span class="font-medium">Use New Address</span>
                  </div>
                </div>
              </div>

              <!-- New Address Form -->
              <div v-if="selectedAddressId === 'new'" class="grid grid-cols-1 md:grid-cols-2 gap-6 animate-fade-in">
                <div class="md:col-span-2">
                  <el-form-item label="Street Address" required>
                    <el-input v-model="formData.street" />
                  </el-form-item>
                </div>
                <div>
                  <el-form-item label="City" required>
                    <el-input v-model="formData.city" />
                  </el-form-item>
                </div>
                <div>
                  <el-form-item label="ZIP Code" required>
                    <el-input v-model="formData.zip" />
                  </el-form-item>
                </div>
                <div class="md:col-span-2">
                  <el-form-item label="Phone" required>
                    <el-input v-model="formData.phone" />
                  </el-form-item>
                </div>
                
                <div v-if="store.user" class="md:col-span-2 pt-2 border-t border-gray-100 dark:border-zinc-800 mt-2">
                    <el-checkbox v-model="saveToAddressBook" label="Save to my Address Book" />
                    <el-input 
                      v-if="saveToAddressBook"
                      placeholder="Address Label (e.g. Work, Gym)" 
                      v-model="formData.addressName"
                      class="mt-3"
                    />
                </div>
              </div>
            </div>
            <el-button 
              type="primary" 
              native-type="submit" 
              size="large" 
              class="w-full !mt-8 !rounded-full !h-12 !text-lg !font-bold"
            >
              Continue to Payment
            </el-button>
          </el-form>
        </div>

        <div v-else class="space-y-8 animate-fade-in">
            <div class="bg-white dark:bg-zinc-900 p-8 rounded-3xl border border-gray-100 dark:border-zinc-800">
              <div class="flex items-center gap-3 mb-6">
              <CreditCard class="text-blue-600" />
              <h2 class="text-xl font-bold">Payment Details</h2>
            </div>
            <div class="p-4 bg-gray-50 dark:bg-black rounded-xl mb-4 border border-gray-200 dark:border-zinc-800">
                <p class="font-medium">Mock Payment Gateway</p>
                <p class="text-sm text-gray-500">No actual payment processing occurs in this demo.</p>
            </div>
            </div>
            <div class="flex gap-4">
              <el-button size="large" round class="flex-1 !h-12" @click="step = 'shipping'">Back</el-button>
              <el-button 
                type="primary" 
                size="large" 
                round 
                class="flex-1 !h-12 !font-bold" 
                @click="handlePlaceOrder"
              >
                Pay ${{ total.toLocaleString() }}
              </el-button>
            </div>
        </div>
      </div>

      <!-- Order Summary -->
      <div>
        <div class="bg-gray-50 dark:bg-zinc-900 p-6 rounded-3xl sticky top-24">
          <h3 class="font-bold text-lg mb-4">Order Summary</h3>
          <div class="space-y-4 mb-6 max-h-60 overflow-y-auto pr-2">
            <div v-for="item in store.cart" :key="item.id" class="flex justify-between items-center text-sm">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-white dark:bg-black rounded-lg flex items-center justify-center">
                    <img :src="item.image" alt="" class="max-w-full max-h-full" />
                </div>
                <span>{{ item.quantity }}x {{ item.name }}</span>
              </div>
              <span class="font-medium">${{ item.price * item.quantity }}</span>
            </div>
          </div>
          <div class="border-t border-gray-200 dark:border-zinc-700 pt-4 space-y-2">
            <div class="flex justify-between text-gray-500">
              <span>Subtotal</span>
              <span>${{ total.toLocaleString() }}</span>
            </div>
            <div class="flex justify-between text-gray-500">
              <span>Shipping</span>
              <span>Free</span>
            </div>
            <div class="flex justify-between font-bold text-xl pt-2">
              <span>Total</span>
              <span>${{ total.toLocaleString() }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>