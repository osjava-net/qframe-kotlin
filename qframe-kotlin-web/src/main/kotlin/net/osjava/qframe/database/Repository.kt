package net.osjava.qframe.database

import jakarta.persistence.criteria.*
import net.osjava.qframe.utils.ClassUtils
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import kotlin.reflect.KClass

@Repository
class Repository(open val sessionFactory: SessionFactory) {

    @Transactional(readOnly = true)
    fun <T : Any> find(page: Page<T> = Page.default(),
                       conditions: (CriteriaBuilder) -> CriteriaQuery<T>
    ): MutableList<T> {
        val query = conditions(getSession().criteriaBuilder)

        return getSession().createQuery(query).setFirstResult(page.offset).setMaxResults(page.size).resultList
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> find(clazz: KClass<T>,
                           page: Page<T> = Page.default(),
                           orderBy: Order = Order.empty(),
                           filter: ((CriteriaBuilder, Root<T>) -> Predicate)? = null): MutableList<T> {
        return find(page) { builder ->
            val query = builder.createQuery(clazz.java)
            val root = query.from(clazz.java)

            if (filter != null) query.where(filter(builder, root))

            orderBy.items.map {
                when (it.asc) {
                    true -> builder.asc(root.get<Expression<*>>(it.key))
                    false -> builder.desc(root.get<Expression<*>>(it.key))
                }
            }.let { query.orderBy(it) }

            query.select(root)
        }
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> findBy(clazz: KClass<T>, property: String, value: Any, page: Page<T> = Page.default(),
                             orderBy: Order = Order.empty()): MutableList<T> {
        return find(clazz = clazz, page = page, orderBy = orderBy) { builder, root ->
            builder.equal(root.get<Expression<*>>(property), value)
        }
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> findFirst(clazz: KClass<T>,
                                orderBy: Order = Order.empty(),
                                filter: ((CriteriaBuilder, Root<T>) -> Predicate)? = null): T? {
        val result = find(clazz, Page(size = 1), orderBy = orderBy, filter = filter)

        return if (result.isNotEmpty()) result[0] else null
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> findFirstBy(clazz: KClass<T>, property: String, value: Any,
                                  orderBy: Order = Order.empty()): T? {
        val result = findBy(clazz, property, value, Page(size = 1), orderBy = orderBy)
        return if (result.isNotEmpty()) result[0] else null
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> findPage(clazz: KClass<T>, page: Page<T> = Page.default(),
                               orderBy: Order = Order.empty(),
                               filter: ((CriteriaBuilder, Root<T>) -> Predicate)? = null): Page<T> {
        val count = count(clazz, filter)

        val result = find(clazz, page, orderBy, filter)

        return Page(page.number, page.size, count, result)
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> count(clazz: KClass<T>,
                            filter: ((CriteriaBuilder, Root<T>) -> Predicate)? = null): Long {
        val builder = getSession().criteriaBuilder
        val query = builder.createQuery(Long::class.java)
        val root = query.from(clazz.java)

        query.select(builder.count(root))
        if (filter != null) query.where(filter(builder, root))

        return getSession().createQuery(query).setMaxResults(1).singleResult
    }

    @Transactional
    fun <T : IEntity> save(entity: T): Serializable {
        getSession().persist(entity)
        getSession().flush()
        return entity.uid()
    }

    @Transactional
    fun <T : IEntity> update(entity: T) {
        getSession().merge(entity)
    }

    @Transactional
    fun <T : IEntity> delete(entity: T) {
        getSession().remove(entity)
    }

    @Transactional(readOnly = true)
    fun <T : IEntity> get(clazz: KClass<T>, uid: Serializable): T? {
        return getSession().get(clazz.java, uid)
    }

    private fun getSession(): Session {
        return sessionFactory.currentSession
    }
}