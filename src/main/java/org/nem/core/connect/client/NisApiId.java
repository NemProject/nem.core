package org.nem.core.connect.client;

import org.nem.core.node.ApiId;

/**
 * NIS REST API paths.
 */
@SuppressWarnings("unused")
public enum NisApiId implements ApiId {
	
	//region account/*

	/**
	 * The /account/unlock API
	 */
	NIS_REST_ACCOUNT_UNLOCK("/account/unlock"),

	/**
	 * The /account/lock API
	 */
	NIS_REST_ACCOUNT_LOCK("/account/lock"),

	/**
	 * The /account/harvests API
	 */
	NIS_REST_ACCOUNT_HARVESTS("/account/harvests"),

	/**
	 * The /account/get API
	 */
	NIS_REST_ACCOUNT_LOOK_UP("/account/get"),

	/**
	 * The /account/get/forwarded API
	 */
	NIS_REST_ACCOUNT_FORWARDED_LOOK_UP("/account/get/forwarded"),

	/**
	 * The /account/get/batch API
	 */
	NIS_REST_ACCOUNT_BATCH_LOOK_UP("/account/get/batch"),

	/**
	 * The /account/mosaic/definition/page API
	 */
	NIS_REST_ACCOUNT_MOSAIC_DEFINITION_PAGE("/account/mosaic/definition/page"),
	
	/**
	 * The /account/namespace/page API
	 */
	NIS_REST_ACCOUNT_NAMESPACE_PAGE("/account/namespace/page"),
	
	
	/**
	 * The /account/mosaic/owned API
	 */
	NIS_REST_ACCOUNT_MOSAIC_OWNED("/account/mosaic/owned"),

	/**
	 * The /account/mosaic/owned/definition/batch API
	 */
	NIS_REST_ACCOUNT_MOSAIC_DEFINITIONS_BATCH_LOOK_UP("/account/mosaic/owned/definition/batch"),
	
	/**
	 * The /account/status API
	 */
	NIS_REST_ACCOUNT_STATUS("/account/status"),

	/**
	 * The /account/transfers/all API
	 */
	NIS_REST_ACCOUNT_TRANSFERS_ALL("/account/transfers/all"),

	/**
	 * The /account/transfers/incoming API
	 */
	NIS_REST_ACCOUNT_TRANSFERS_INCOMING("/account/transfers/incoming"),

	/**
	 * The /account/transfers/outgoing API
	 */
	NIS_REST_ACCOUNT_TRANSFERS_OUTGOING("/account/transfers/outgoing"),

	/**
	 * The /account/transfers API
	 */
	NIS_REST_ACCOUNT_TRANSFERS("/account/transfers"),

	/**
	 * The /account/unconfirmedTransactions API
	 */
	NIS_REST_ACCOUNT_UNCONFIRMED("/account/unconfirmedTransactions"),

	//endregion

	//region block/*

	/**
	 * The /block/at/public API
	 */
	NIS_REST_BLOCK_AT_PUBLIC("/block/at/public"),


	/**
	 * The /local/chain/blocks-after API
	 */
	NIS_REST_BLOCK_AFTER_LOCAL("/local/chain/blocks-after"),

	//endregion
	
	//namespace and mosaics /*
	
	/**
	 * The /namespace/root/page API
	 */
	NIS_REST_NAMESPACE_ROOT_PAGE("/namespace/root/page"),
	
	/**
	 * The /namespace API
	 */
	NIS_REST_NAMESPACE("/namespace"),
	
	/**
	 * The /namespace/mosaic/definition/page API
	 */
	NIS_REST_NAMESPACE_MOSAIC_DEFINITION_PAGE("/namespace/mosaic/definition/page"),
	

	//region chain/*

	/**
	 * The /chain/last-block API.
	 */
	NIS_REST_CHAIN_LAST_BLOCK("/chain/last-block"),

	/**
	 * The /chain/height API.
	 */
	NIS_REST_CHAIN_HEIGHT("/chain/height"),
	
	/**
	 * The /chain/score API.
	 */
	NIS_REST_CHAIN_SCORE("/chain/score"),

	//endregion

	//region node/*

	/**
	 * The node/extended-info API.
	 */
	NIS_REST_NODE_EXTENDED_INFO("/node/extended-info"),

	/**
	 * The node/info API.
	 */
	NIS_REST_NODE_INFO("/node/info"),

	/**
	 * The node/peer-list/all API.
	 */
	NIS_REST_PEER_LIST("/node/peer-list/all"),

	/**
	 * The node/peer-list/active API.
	 */
	NIS_REST_PEER_LIST_ACTIVE("/node/peer-list/active"),

	/**
	 * The /node/boot API.
	 */
	NIS_REST_NODE_BOOT("/node/boot"),

	/**
	 * The node/active-peers/max-chain-height API.
	 */
	NIS_REST_ACTIVE_PEERS_MAX_CHAIN_HEIGHT("/node/active-peers/max-chain-height"),

	//endregion

	//region transaction/*

	/**
	 * The /transaction/announce API.
	 */
	NIS_REST_TRANSACTION_ANNOUNCE("/transaction/announce"),

	/**
	 * The /transaction/get API
	 */
	NIS_REST_TRANSACTION_GET("/transaction/get"),

	//endregion

	// region /*

	/**
	 * The /heartbeat API.
	 */
	NIS_REST_HEARTBEAT("/heartbeat"),

	/**
	 * The /shutdown API.
	 */
	NIS_REST_SHUTDOWN("/shutdown"),

	/**
	 * The /status API.
	 */
	NIS_REST_STATUS("/status"),

	//endregion

	//region time-sync/*

	/**
	 * The time-sync/network-time API
	 */
	NIS_REST_TIME_SYNC_NETWORK_TIME("time-sync/network-time");

	//endregion
	
	private final String value;

	/**
	 * Creates a NIS API id.
	 *
	 * @param value The string representation.
	 */
	NisApiId(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
